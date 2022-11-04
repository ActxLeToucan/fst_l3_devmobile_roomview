package fr.antoinectx.roomview.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

public class Zone {
    private String id = UUID.randomUUID().toString();
    private String nom;
    private final Date dateCapture;
    private final Photo[] photos;

    private Zone(String id, String nom, Date dateCapture, Photo[] photos) {
        this.id = id;
        this.nom = nom;
        this.dateCapture = dateCapture;
        this.photos = photos;
    }

    public Zone(String nom, Date dateCapture) {
        this.nom = nom;
        this.dateCapture = dateCapture;
        this.photos = new Photo[4];
    }

    public String getId() {
        return id;
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

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("nom", nom);
            json.put("dateCapture", dateCapture.getTime());
            json.put("photoNord", photoNord().toJSON());
            json.put("photoSud", photoSud().toJSON());
            json.put("photoEst", photoEst().toJSON());
            json.put("photoOuest", photoOuest().toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static Zone fromJSON(JSONObject json) {
        try {
            return new Zone(
                    json.getString("id"),
                    json.getString("nom"),
                    new Date(json.getLong("dateCapture")),
                    new Photo[] {
                            Photo.fromJSON(json.getJSONObject("photoNord")),
                            Photo.fromJSON(json.getJSONObject("photoSud")),
                            Photo.fromJSON(json.getJSONObject("photoEst")),
                            Photo.fromJSON(json.getJSONObject("photoOuest"))
                    }
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
