package fr.antoinectx.roomview.models;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

public class Area extends ManipulateFiles {
    /**
     * The area unique ID
     */
    private final String id;
    /**
     * The parent building unique ID
     */
    private final String buildingId;
    /**
     * The orientation photos (one for each direction) [N, E, S, W]
     */
    private final OrientationPhoto[] orientationPhotos;
    /**
     * The area name
     */
    private String name;

    /**
     * Complete constructor, only used when loading an area from JSON
     *
     * @param id                The area unique ID
     * @param buildingId        The parent building unique ID
     * @param name              The area name
     * @param orientationPhotos The orientation photos [N, E, S, W]
     */
    private Area(String id, String buildingId, String name, OrientationPhoto[] orientationPhotos) {
        this.id = id;
        this.buildingId = buildingId;
        this.name = name;
        this.orientationPhotos = orientationPhotos;
    }

    public Area(String buildingId, String name) {
        this.id = UUID.randomUUID().toString();
        this.buildingId = buildingId;
        this.name = name;
        this.orientationPhotos = new OrientationPhoto[4];
    }

    /**
     * Convert a JSON string to an area
     *
     * @param json The JSON string
     * @return The area
     */
    public static Area fromJSONString(String json) {
        try {
            return fromJSON(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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
                    json.getString("buildingId"),
                    json.getString("name"),
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

    /**
     * Get the directory of the area (where the photos are stored), create it if it doesn't exist.
     *
     * @param context The context of the application
     * @return The directory of the area
     */
    public File getDirectory(Context context) {
        File directory = new File(Building.getDirectory(context, buildingId), id);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e("Area", "Failed to create directory " + directory.getAbsolutePath());
            }
        }
        return directory;
    }

    public String getId() {
        return id;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrientationPhoto[] getOrientationPhotos() {
        return orientationPhotos;
    }

    @Nullable
    public OrientationPhoto getNorth() {
        return orientationPhotos[0];
    }

    public void setNorth(OrientationPhoto orientationPhoto) {
        orientationPhotos[0] = orientationPhoto;
    }

    @Nullable
    public File getNorthFile(Context context) {
        File directory = getDirectory(context);
        if (getNorth() == null) return null;
        if (getNorth().getFilename() == null) return null;
        return new File(directory, getNorth().getFilename());
    }

    @Nullable
    public OrientationPhoto getEast() {
        return orientationPhotos[1];
    }

    public void setEast(OrientationPhoto orientationPhoto) {
        orientationPhotos[1] = orientationPhoto;
    }

    @Nullable
    public File getEastFile(Context context) {
        File directory = getDirectory(context);
        if (getEast() == null) return null;
        if (getEast().getFilename() == null) return null;
        return new File(directory, getEast().getFilename());
    }

    @Nullable
    public OrientationPhoto getSouth() {
        return orientationPhotos[2];
    }

    public void setSouth(OrientationPhoto orientationPhoto) {
        orientationPhotos[2] = orientationPhoto;
    }

    @Nullable
    public File getSouthFile(Context context) {
        File directory = getDirectory(context);
        if (getSouth() == null) return null;
        if (getSouth().getFilename() == null) return null;
        return new File(directory, getSouth().getFilename());
    }

    @Nullable
    public OrientationPhoto getWest() {
        return orientationPhotos[3];
    }

    public void setWest(OrientationPhoto orientationPhoto) {
        orientationPhotos[3] = orientationPhoto;
    }

    @Nullable
    public File getWestFile(Context context) {
        File directory = getDirectory(context);
        if (getWest() == null) return null;
        if (getWest().getFilename() == null) return null;
        return new File(directory, getWest().getFilename());
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
            json.put("buildingId", buildingId);
            json.put("name", name);
            if (getNorth() != null) json.put("north", getNorth().toJSON());
            if (getEast() != null) json.put("east", getEast().toJSON());
            if (getSouth() != null) json.put("south", getSouth().toJSON());
            if (getWest() != null) json.put("west", getWest().toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Delete the files of the area
     *
     * @param context The context of the application
     */
    public void delete(Context context) {
        File directory = getDirectory(context);
        if (!directory.exists()) {
            Log.e("Area", "delete: Directory " + directory.getAbsolutePath() + " does not exist");
            return;
        }

        deleteRecursive(directory);
        directory.delete();
    }
}
