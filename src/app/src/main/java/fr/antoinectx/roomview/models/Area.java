package fr.antoinectx.roomview.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

public class Area {
    /**
     * The area unique ID
     */
    private final String id;
    /**
     * The area name
     */
    private String name;
    /**
     * The date of capture
     */
    private final Date dateCapture;
    /**
     * The orientation photos (one for each direction) [N, E, S, W]
     */
    private final OrientationPhoto[] orientationPhotos;

    /**
     * Complete constructor, only used when loading an area from JSON
     *
     * @param id                The area unique ID
     * @param name              The area name
     * @param dateCapture       The date of capture
     * @param orientationPhotos The orientation photos [N, E, S, W]
     */
    private Area(String id, String name, Date dateCapture, OrientationPhoto[] orientationPhotos) {
        this.id = id;
        this.name = name;
        this.dateCapture = dateCapture;
        this.orientationPhotos = orientationPhotos;
    }

    public Area(String name, Date dateCapture) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.dateCapture = dateCapture;
        this.orientationPhotos = new OrientationPhoto[4];
    }

    public String getId() {
        return id;
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

    public OrientationPhoto[] getPhotos() {
        return orientationPhotos;
    }

    public OrientationPhoto north() {
        return orientationPhotos[0];
    }

    public OrientationPhoto east() {
        return orientationPhotos[1];
    }

    public OrientationPhoto south() {
        return orientationPhotos[2];
    }

    public OrientationPhoto west() {
        return orientationPhotos[3];
    }

    public void setNorth(OrientationPhoto orientationPhoto) {
        orientationPhotos[0] = orientationPhoto;
    }

    public void setEast(OrientationPhoto orientationPhoto) {
        orientationPhotos[1] = orientationPhoto;
    }

    public void setSouth(OrientationPhoto orientationPhoto) {
        orientationPhotos[2] = orientationPhoto;
    }

    public void setWest(OrientationPhoto orientationPhoto) {
        orientationPhotos[3] = orientationPhoto;
    }

    /**
     * Convert the area to a JSON object
     *
     * @return The JSON object
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("dateCapture", dateCapture.getTime());
            if (north() != null) json.put("north", north().toJSON());
            if (east() != null) json.put("east", east().toJSON());
            if (south() != null) json.put("south", south().toJSON());
            if (west() != null) json.put("west", west().toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Create an area from a JSON object
     *
     * @param json The JSON object
     * @return The area
     */
    public static Area fromJSON(JSONObject json) {
        try {
            return new Area(
                    json.getString("id"),
                    json.getString("name"),
                    new Date(json.getLong("dateCapture")),
                    new OrientationPhoto[]{
                            OrientationPhoto.fromJSON(json.optJSONObject("north")),
                            OrientationPhoto.fromJSON(json.optJSONObject("east")),
                            OrientationPhoto.fromJSON(json.optJSONObject("south")),
                            OrientationPhoto.fromJSON(json.optJSONObject("west"))
                    }
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
