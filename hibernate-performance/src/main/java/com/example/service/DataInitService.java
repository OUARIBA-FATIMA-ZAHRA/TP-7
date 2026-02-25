package com.example.service;

import com.example.model.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class DataInitService {

    private EntityManagerFactory emf;

    public DataInitService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void initData() {

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Categorie roman = new Categorie("Roman");
        Categorie sf = new Categorie("Science Fiction");

        em.persist(roman);
        em.persist(sf);

        Auteur hugo = new Auteur("Hugo","Victor","hugo@test.com");

        for(int i=1;i<=15;i++){
            Livre livre = new Livre("Roman Hugo "+i, 1800+i);
            livre.addCategorie(roman);
            hugo.addLivre(livre);
        }

        Auteur asimov = new Auteur("Asimov","Isaac","asimov@test.com");

        for(int i=1;i<=15;i++){
            Livre livre = new Livre("SF Asimov "+i, 1950+i);
            livre.addCategorie(sf);
            asimov.addLivre(livre);
        }

        em.persist(hugo);
        em.persist(asimov);

        em.getTransaction().commit();
        em.close();

        System.out.println("Données insérées avec succès !");
    }
}