package fr.antoinectx.roomview.models;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Building {
    private final String id;
    private String name;
    private String description;
    private final List<Area> areas;
    private String photo;

    /**
     * Complete constructor, only used when loading a building from a file
     * @param id The unique ID of the building
     * @param name The name of the building
     * @param description The description of the building
     * @param areas The zones of the building
     * @param photo The file name of the photo of the building
     */
    private Building(String id, String name, String description, List<Area> areas, String photo) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.areas = areas;
        this.photo = photo;
    }

    public Building(String name, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.areas = new ArrayList<>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Area> getAreas() {
        return areas;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    // --- Save & Load ---

    /**
     * Save the building to a JSON file
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity),
     *                used to get the files directory.
     *                The file will be saved in {@literal /data/data/<package_name>/files/<id>/data.json}
     *                where {@literal <id>} is the unique ID of the building
     */
    public void save(Context context) {
        File dir = new File(context.getFilesDir() + "/" + id);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("Building", "Failed to create directory " + dir.getAbsolutePath());
                return;
            }
        }

        File file = new File(dir.getAbsolutePath(), "data.json");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(toJSON().toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("Building", "Saved " + name + " to " + file.getAbsolutePath());
    }

    /**
     * Convert the building to a JSON object
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("description", description);
            json.put("photo", photo);
            JSONArray areasJSON = new JSONArray();
            for (Area area : areas) {
                areasJSON.put(area.toJSON());
            }
            json.put("areas", areasJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Load all the buildings from the files directory
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity),
     * @return A list of all the buildings
     */
    public static List<Building> loadAll(Context context) {
        File dir = new File(context.getFilesDir().toString());

        List<Building> buildings = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                Building building = load(context, file.getName());
                if (building != null) {
                    buildings.add(building);
                }
            }
        }

        return buildings;
    }

    /**
     * Load a building from a JSON file
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     * @param id The unique ID of the building to load
     * @return The building
     */
    public static Building load(Context context, String id) {
        File dir = new File(context.getFilesDir() + "/" + id);
        if (!dir.exists()) {
            Log.e("Building", "load: Impossible de trouver le dossier " + dir.getAbsolutePath());
            return null;
        }

        File file = new File(dir.getAbsolutePath(), "data.json");

        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Building building = fromJSONString(new String(bytes));
        if (building == null) {
            Log.e("Building", "load: Impossible de charger le fichier " + file.getAbsolutePath());
            return null;
        } else {
            Log.d("Building", "load: Chargé depuis " + file.getAbsolutePath());
            return building;
        }
    }

    /**
     * Convert a JSON string to a building
     * @param json The JSON string
     * @return The building
     */
    public static Building fromJSONString(String json) {
        try {
            return fromJSON(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a building from a JSON object
     * @param json The JSON object
     * @return The building
     */
    public static Building fromJSON(JSONObject json) {
        try {
            List<Area> areas = new ArrayList<>();
            for (int i = 0; i < json.getJSONArray("areas").length(); i++) {
                areas.add(Area.fromJSON(json.getJSONArray("areas").getJSONObject(i)));
            }

            // when all zones are loaded, we can set the autreCote of each passage in zone's photo from json
            for (Area area : areas) {
                for (OrientationPhoto orientationPhoto : area.getPhotos()) {
                    for (Passage passage : orientationPhoto.getPassages()) {
                        passage.setAutreCote(areas.stream()
                                .filter(z -> z.getId().equals(passage.getAutreCoteId()))
                                .findFirst()
                                .orElse(null));
                    }
                }
            }

            return new Building(
                    json.getString("id"),
                    json.getString("name"),
                    json.getString("description"),
                    areas,
                    json.optString("photo")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete a building from the files directory
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     */
    public void delete(Context context) {
        File dir = new File(context.getFilesDir() + "/" + id);
        if (!dir.exists()) {
            Log.e("Building", "delete: Impossible de trouver le dossier " + dir.getAbsolutePath());
            return;
        }

        deleteRecursive(dir);
    }

    /**
     * Delete a file or a directory recursively
     * @param fileOrDirectory The file or directory to delete
     */
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                deleteRecursive(child);
            }
        }
        if (!fileOrDirectory.delete()) {
            Log.e("Building", "deleteRecursive: Impossible de supprimer " + fileOrDirectory.getAbsolutePath());
        }
    }
}
