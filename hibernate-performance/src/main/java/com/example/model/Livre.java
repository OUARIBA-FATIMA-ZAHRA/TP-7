package com.example.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cacheable
@NamedEntityGraph(
        name = "graph.Livre.categoriesEtAuteur",
        attributeNodes = {
                @NamedAttributeNode("categories"),
                @NamedAttributeNode("auteur")
        }
)
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    private Integer anneePublication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id")
    private Auteur auteur;

    @ManyToMany
    @JoinTable(
            name = "livre_categorie",
            joinColumns = @JoinColumn(name = "livre_id"),
            inverseJoinColumns = @JoinColumn(name = "categorie_id")
    )
    private Set<Categorie> categories = new HashSet<>();

    public Livre() {}

    public Livre(String titre, Integer anneePublication) {
        this.titre = titre;
        this.anneePublication = anneePublication;
    }

    public void setAuteur(Auteur auteur) {
        this.auteur = auteur;
    }

    public void addCategorie(Categorie categorie){
        categories.add(categorie);
    }


    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public Integer getAnneePublication() {
        return anneePublication;
    }

    public void setAnneePublication(Integer anneePublication) {
        this.anneePublication = anneePublication;
    }

    public Auteur getAuteur() {
        return auteur;
    }

    public Set<Categorie> getCategories() {
        return categories;
    }
}