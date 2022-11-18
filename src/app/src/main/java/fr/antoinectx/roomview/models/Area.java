package fr.antoinectx.roomview.models;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
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
                            !json.isNull("north") ? OrientationPhoto.fromJSON(json.getJSONObject("north")) : null,
                            !json.isNull("east") ? OrientationPhoto.fromJSON(json.getJSONObject("east")) : null,
                            !json.isNull("south") ? OrientationPhoto.fromJSON(json.getJSONObject("south")) : null,
                            !json.isNull("west") ? OrientationPhoto.fromJSON(json.getJSONObject("west")) : null
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

    public OrientationPhoto getOrientationPhoto(@NonNull Orientation orientation) {
        return orientationPhotos[orientation.ordinal()];
    }

    public void setOrientationPhoto(@NonNull Orientation orientation, OrientationPhoto orientationPhoto) {
        orientationPhotos[orientation.ordinal()] = orientationPhoto;
    }

    @Nullable
    public File getFile(Context context, @NonNull Orientation orientation) {
        File directory = getDirectory(context);
        OrientationPhoto orientationPhoto = getOrientationPhoto(orientation);
        if (orientationPhoto == null) return null;
        return new File(directory, orientationPhoto.getFilename());
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
            json.put("north", getOrientationPhoto(Orientation.NORTH) == null ? JSONObject.NULL : getOrientationPhoto(Orientation.NORTH).toJSON());
            json.put("east", getOrientationPhoto(Orientation.EAST) == null ? JSONObject.NULL : getOrientationPhoto(Orientation.EAST).toJSON());
            json.put("south", getOrientationPhoto(Orientation.SOUTH) == null ? JSONObject.NULL : getOrientationPhoto(Orientation.SOUTH).toJSON());
            json.put("west", getOrientationPhoto(Orientation.WEST) == null ? JSONObject.NULL : getOrientationPhoto(Orientation.WEST).toJSON());
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

    /**
     * Reload the area from the building
     *
     * @param building The building
     * @return Whether the area was found in the building
     */
    public boolean reloadFromBuilding(Building building) {
        if (!building.getId().equals(buildingId)) {
            Log.e("Area", "reloadFromBuilding: The building ID does not match");
            return false;
        }

        Area area = building.getAreas()
                .stream()
                .filter(a -> a.getId().equals(id))
                .findFirst().orElse(null);
        if (area == null) return false;

        this.name = area.name;
        System.arraycopy(area.orientationPhotos, 0, this.orientationPhotos, 0, this.orientationPhotos.length);
        return true;
    }
}
