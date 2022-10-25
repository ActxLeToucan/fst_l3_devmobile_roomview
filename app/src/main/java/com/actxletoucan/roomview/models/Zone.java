package com.actxletoucan.roomview.models;

import java.util.Date;

public class Zone {
    private String nom;
    private final Date dateCapture;
    private final Photo[] photos;

    public Zone(String nom, Date dateCapture) {
        this.nom = nom;
        this.dateCapture = dateCapture;
        this.photos = new Photo[4];
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Date getDateCapture() {
        return dateCapture;
    }

    public Photo photoNord() {
        return photos[0];
    }

    public Photo photoSud() {
        return photos[1];
    }

    public Photo photoEst() {
        return photos[2];
    }

    public Photo photoOuest() {
        return photos[3];
    }

    public void setPhotoNord(Photo photo) {
        photos[0] = photo;
    }

    public void setPhotoSud(Photo photo) {
        photos[1] = photo;
    }

    public void setPhotoEst(Photo photo) {
        photos[2] = photo;
    }

    public void setPhotoOuest(Photo photo) {
        photos[3] = photo;
    }
}
