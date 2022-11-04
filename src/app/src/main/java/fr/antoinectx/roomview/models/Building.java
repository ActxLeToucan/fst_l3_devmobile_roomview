package fr.antoinectx.roomview.models;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Building implements Serializable {
    private final UUID id = UUID.randomUUID();
    private String name;
    private String description;
    private final List<Area> areas;

    public Building(String name, String description) {
        this.name = name;
        this.description = description;
        this.areas = new ArrayList<>();
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

    // --- Save & Load ---

    /**
     * Save the building to a file
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity),
     *                used to get the files directory.
     *                The file will be saved in {@literal /data/data/<package_name>/files/<id>.building}
     *                where {@literal <id>} is the unique ID of the building
     */
    public void save(Context context) {
        File file = new File(context.getFilesDir(), id + ".building");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Log.d("Batiment", "Saved " + name + " to " + file.getAbsolutePath());
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
            if (file.getName().endsWith(".building")) {
                buildings.add(load(context, file.getName().replace(".building", "")));
            }
        }

        return buildings;
    }

    /**
     * Load a building with its id
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     * @param id The unique ID of the building to load
     * @return The building
     */
    public static Building load(Context context, String id) {
        File file = new File(context.getFilesDir(), id + ".building");
        if (!file.exists()) {
            Log.e("Batiment", "load: Impossible de trouver le fichier " + file.getAbsolutePath());
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Building) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
