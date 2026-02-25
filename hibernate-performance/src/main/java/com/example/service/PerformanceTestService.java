package com.example.service;

import com.example.model.Auteur;
import com.example.model.Livre;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityGraph;
import javax.persistence.TypedQuery;
import java.util.List;

public class PerformanceTestService {

    private final EntityManagerFactory emf;

    public PerformanceTestService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void resetStatistics() {
        Session session = emf.createEntityManager().unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.clear();
    }

    public void printStatistics(String testName) {
        Session session = emf.createEntityManager().unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();

        System.out.println("\n=== Statistiques pour " + testName + " ===");
        System.out.println("Requêtes exécutées: " + stats.getQueryExecutionCount());
        System.out.println("Entités chargées: " + stats.getEntityLoadCount());
        System.out.println("Hits cache 2nd niveau: " + stats.getSecondLevelCacheHitCount());
        System.out.println("Miss cache 2nd niveau: " + stats.getSecondLevelCacheMissCount());
        double hitRatio = (stats.getSecondLevelCacheHitCount() + stats.getSecondLevelCacheMissCount() > 0) ?
                (double) stats.getSecondLevelCacheHitCount() / (stats.getSecondLevelCacheHitCount() + stats.getSecondLevelCacheMissCount()) : 0;
        System.out.println("Ratio de hit du cache: " + hitRatio);
    }

    public void testN1Problem() {
        resetStatistics();
        long start = System.currentTimeMillis();

        EntityManager em = emf.createEntityManager();
        try {
            List<Auteur> auteurs = em.createQuery("SELECT a FROM Auteur a", Auteur.class).getResultList();

            for (Auteur auteur : auteurs) {
                System.out.println("Auteur: " + auteur.getNom() + " " + auteur.getPrenom());
                System.out.println("Nombre de livres: " + auteur.getLivres().size());
                for (Livre livre : auteur.getLivres()) {
                    System.out.println("  - " + livre.getTitre() + " (" + livre.getAnneePublication() + ")");
                    System.out.println("    Catégories: " + livre.getCategories().size());
                }
            }
        } finally {
            em.close();
        }

        long end = System.currentTimeMillis();
        System.out.println("Temps d'exécution: " + (end - start) + "ms");
        printStatistics("Problème N+1 sans optimisation");
    }

    public void testJoinFetch() {
        resetStatistics();
        long start = System.currentTimeMillis();

        EntityManager em = emf.createEntityManager();
        try {
            List<Auteur> auteurs = em.createQuery(
                    "SELECT DISTINCT a FROM Auteur a LEFT JOIN FETCH a.livres", Auteur.class
            ).getResultList();

            for (Auteur auteur : auteurs) {
                System.out.println("Auteur: " + auteur.getNom() + " " + auteur.getPrenom());
                for (Livre livre : auteur.getLivres()) {
                    System.out.println("  - " + livre.getTitre() + " (" + livre.getAnneePublication() + ")");
                    System.out.println("    Catégories: " + livre.getCategories().size());
                }
            }
        } finally {
            em.close();
        }

        long end = System.currentTimeMillis();
        System.out.println("Temps d'exécution: " + (end - start) + "ms");
        printStatistics("Résolution avec JOIN FETCH");
    }

    public void testEntityGraph() {
        resetStatistics();
        long start = System.currentTimeMillis();

        EntityManager em = emf.createEntityManager();
        try {
            EntityGraph<?> graph = em.getEntityGraph("graph.Livre.categoriesEtAuteur");
            List<Livre> livres = em.createQuery("SELECT l FROM Livre l", Livre.class)
                    .setHint("javax.persistence.fetchgraph", graph)
                    .getResultList();

            for (Livre livre : livres) {
                System.out.println("Livre: " + livre.getTitre() + " (" + livre.getAnneePublication() + ")");
                System.out.println("  Auteur: " + livre.getAuteur().getNom());
                System.out.println("  Catégories: " + livre.getCategories().size());
            }
        } finally {
            em.close();
        }

        long end = System.currentTimeMillis();
        System.out.println("Temps d'exécution: " + (end - start) + "ms");
        printStatistics("Résolution avec Entity Graphs");
    }

    public void testSecondLevelCache() {
        System.out.println("\n=== Test cache de second niveau ===");

        resetStatistics();
        EntityManager em1 = emf.createEntityManager();
        try {
            Auteur auteur = em1.find(Auteur.class, 1L);
            System.out.println("Auteur trouvé: " + auteur.getNom());
        } finally {
            em1.close();
        }
        printStatistics("Premier accès");

        resetStatistics();
        EntityManager em2 = emf.createEntityManager();
        try {
            Auteur auteur = em2.find(Auteur.class, 1L);
            System.out.println("Auteur trouvé: " + auteur.getNom());
        } finally {
            em2.close();
        }
        printStatistics("Deuxième accès");
    }

    public void testPerformanceComparison() {
        System.out.println("\n=== Comparaison performances avec/sans cache ===");

        EntityManager em = emf.createEntityManager();
        try {
            em.unwrap(Session.class).getSessionFactory().getCache().evictAllRegions();
            long start = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                Auteur auteur = em.find(Auteur.class, (i % 4) + 1L);
                auteur.getLivres().size();
            }
            long end = System.currentTimeMillis();
            System.out.println("Temps sans cache: " + (end - start) + "ms");
        } finally {
            em.close();
        }

        resetStatistics();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            EntityManager em2 = emf.createEntityManager();
            try {
                Auteur auteur = em2.find(Auteur.class, (i % 4) + 1L);
                auteur.getLivres().size();
            } finally {
                em2.close();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Temps avec cache: " + (end - start) + "ms");
        printStatistics("Test avec cache");
    }
}
