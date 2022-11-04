package fr.antoinectx.roomview.models;

import java.io.Serializable;
import java.util.Date;

public class Area implements Serializable {
    private String name;
    private final Date dateCapture;
    private final Photo[] photos;

    public Area(String name, Date dateCapture) {
        this.name = name;
        this.dateCapture = dateCapture;
        this.photos = new Photo[4];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateCapture() {
        return dateCapture;
    }

    public Photo[] getPhotos() {
        return photos;
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
