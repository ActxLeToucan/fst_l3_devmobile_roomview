package com.actxletoucan.roomview.models;

import java.util.ArrayList;
import java.util.List;

public class Batiment {
    private String nom;
    private String description;
    private final List<Zone> zones;

    public Batiment(String nom) {
        this.nom = nom;
        this.zones = new ArrayList<>();
    }

    public Batiment(String nom, String description) {
        this.nom = nom;
        this.description = description;
        this.zones = new ArrayList<>();
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Zone> getZones() {
        return zones;
    }
}
