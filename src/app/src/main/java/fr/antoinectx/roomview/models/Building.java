package fr.antoinectx.roomview.models;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.jgrapht.graph.DirectedMultigraph;
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

public class Building extends ManipulateFiles {
    /**
     * Unique ID of the building
     */
    private final String id;
    /**
     * Areas of the building (rooms, hallways, etc.)
     */
    private List<Area> areas;
    /**
     * Name of the building
     */
    private String name;
    /**
     * Description of the building
     */
    private String description;
    /**
     * Photo filename of the building
     */
    @Nullable
    private String photoPath;

    /**
     * Complete constructor, only used when restoring a building from a file
     *
     * @param id          The unique ID of the building
     * @param name        The name of the building
     * @param description The description of the building
     * @param areas       The zones of the building
     * @param photoPath   The file name of the photo of the building
     */
    private Building(String id, String name, String description, List<Area> areas, @Nullable String photoPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.areas = areas;
        this.photoPath = photoPath;
    }

    /**
     * Default constructor
     *
     * @param name        The name of the building
     * @param description The description of the building
     */
    public Building(String name, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.areas = new ArrayList<>();
    }

    /**
     * Load all the buildings from the files directory
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity),
     * @return A list of all the buildings
     */
    @NonNull
    public static List<Building> loadAll(@NonNull Context context) {
        File dir = new File(context.getFilesDir().toString());

        List<Building> buildings = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                Building building = load(context, file.getName(), false);
                if (building != null) {
                    buildings.add(building);
                }
            }
        }

        return buildings;
    }

    /**
     * Load a building from a JSON file
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     * @param id      The unique ID of the building to load
     * @param forceId If true, the id parameter will be used as the building ID
     * @return The building
     */
    @Nullable
    public static Building load(@NonNull Context context, String id, boolean forceId) {
        File dir = new File(context.getFilesDir() + "/" + id);
        if (!dir.exists()) {
            Log.e("Building", "load: Can not find directory " + dir.getAbsolutePath());
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

        Building building = forceId ? fromJSONString(new String(bytes), id) : fromJSONString(new String(bytes));
        if (building == null) {
            Log.e("Building", "load: Can not parse JSON file " + file.getAbsolutePath());
            return null;
        } else {
            Log.d("Building", "load: Loaded " + building.getName() + " from " + file.getAbsolutePath());
            return building;
        }
    }

    /**
     * Convert a JSON string to a building
     *
     * @param json The JSON string
     * @return The building
     * @see #fromJSONString(String, String)
     * @see #fromJSON(JSONObject)
     * @see #fromJSON(JSONObject, String)
     */
    @Nullable
    public static Building fromJSONString(String json) {
        try {
            return fromJSON(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert a JSON string to a building with a specific ID
     *
     * @param json The JSON string
     * @param id   The ID of the building
     * @return The building
     * @see #fromJSONString(String)
     * @see #fromJSON(JSONObject)
     * @see #fromJSON(JSONObject, String)
     */
    @Nullable
    public static Building fromJSONString(String json, String id) {
        try {
            return fromJSON(new JSONObject(json), id);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a building from a JSON object
     *
     * @param json The JSON object
     * @return The building
     * @see Building#fromJSON(JSONObject, String)
     * @see Building#fromJSONString(String)
     * @see Building#fromJSONString(String, String)
     */
    @Nullable
    public static Building fromJSON(JSONObject json) {
        try {
            return fromJSON(json, json.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a building from a JSON object with a specific ID
     *
     * @param json The JSON object
     * @param id   The ID of the building
     * @return The building
     * @see Building#fromJSON(JSONObject)
     * @see Building#fromJSONString(String)
     * @see Building#fromJSONString(String, String)
     */
    @Nullable
    public static Building fromJSON(JSONObject json, String id) {
        try {
            List<Area> areas = new ArrayList<>();
            for (int i = 0; i < json.getJSONArray("areas").length(); i++) {
                areas.add(Area.fromJSON(json.getJSONArray("areas").getJSONObject(i), id));
            }

            return new Building(
                    id,
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
     * Get the directory of a building
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     * @param id      The unique ID of the building
     * @return The directory
     */
    @NonNull
    @Contract("_, _ -> new")
    public static File getDirectory(@NonNull Context context, @NonNull String id) {
        return new File(context.getFilesDir(), id);
    }

    /**
     * Import a building from a zip file
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     * @param uri     The URI of the zip file to import from
     * @return Whether the import was successful
     */
    public static boolean importFrom(Context context, Uri uri) {
        File dir = new File(context.getFilesDir(), UUID.randomUUID().toString());

        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd == null) return false;

            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("Building", "Failed to create directory " + dir.getAbsolutePath());
                    return false;
                }
            }

            unzip(pfd, dir);

            Building building = load(context, dir.getName(), true);
            if (building == null) {
                deleteRecursive(dir, "Building.importFrom");
                return false;
            }
            building.save(context);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (dir.exists()) {
                deleteRecursive(dir, "Building.importFrom");
            }
            return false;
        }
    }

    /**
     * Get the photo file
     *
     * @param context The context of the application
     *                (used to get the directory of the application)
     * @return The photo file
     */
    public File getPhotoFile(Context context) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            return null;
        }
        return new File(getDirectory(context), photoPath);
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

    @Nullable
    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(@Nullable String photoPath) {
        this.photoPath = photoPath;
    }

    /**
     * Save the building to a JSON file
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity),
     *                used to get the files directory.
     *                The file will be saved in {@literal /data/data/<package_name>/files/<id>/data.json}
     *                where {@literal <id>} is the unique ID of the building
     */
    public void save(@NonNull Context context) {
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
            if (photoPath != null) json.put("photo", photoPath);
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
     * Delete a building from the files directory
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     */
    public void delete(Context context) {
        File dir = getDirectory(context);
        if (!dir.exists()) {
            Log.e("Building", "delete: Can not find directory " + dir.getAbsolutePath());
            return;
        }

        deleteRecursive(dir, "Building.delete");
    }

    /**
     * Get the directory of the building
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     * @return The directory
     */
    public File getDirectory(Context context) {
        return getDirectory(context, id);
    }

    /**
     * Reload the building from the JSON file
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     * @return Whether the building was successfully reloaded.
     * False indicates that the building does not exist anymore.
     */
    public boolean reload(Context context) {
        Building building = load(context, id, false);
        if (building != null) {
            this.name = building.name;
            this.description = building.description;
            this.areas = building.areas;
            this.photoPath = building.photoPath;
        }
        return building != null;
    }

    /**
     * Export the building folder to a zip file
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     * @param uri     The URI of the zip file to export to
     * @return Whether the export was successful
     */
    public boolean export(Context context, Uri uri) {
        try (ParcelFileDescriptor pfd = context.getContentResolver().
                openFileDescriptor(uri, "w")) {
            if (pfd == null) return false;

            return zip(getDirectory(context), pfd, false);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get the graph corresponding to the building
     *
     * @return The graph
     */
    public DirectedMultigraph<Area, Passage> getGraph() {
        DirectedMultigraph<Area, Passage> graph = new DirectedMultigraph<>(Passage.class);
        for (Area area : areas) {
            graph.addVertex(area);
        }
        for (Area area : areas) {
            for (DirectionPhoto directionPhoto : area.getDirectionPhotos()) {
                if (directionPhoto == null) continue;
                for (Passage passage : directionPhoto.getPassages()) {
                    graph.addEdge(area, passage.getOtherSide(areas), passage);
                }
            }
        }
        return graph;
    }
}
