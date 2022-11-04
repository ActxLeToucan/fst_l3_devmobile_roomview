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
    private String id = UUID.randomUUID().toString();
    private String nom;
    private String description;
    private final List<Zone> zones;

    public Building(String nom, String description) {
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

    // --- Save & Load ---

    public void save(Context context) {
        File dir = new File(context.getFilesDir() + "/" + id);
        if (!dir.mkdirs()) {
            Log.e("Batiment", "save: Impossible de créer le dossier " + dir.getAbsolutePath());
            return;
        }

        File file = new File(dir.getAbsolutePath(), "data.ser");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Log.d("Batiment", "Saved " + nom + " to " + file.getAbsolutePath());
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
     * Load a building with its id
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity)
     * @param id The unique ID of the building to load
     * @return The building
     */
    public static Building load(Context context, String id) {
        File dir = new File(context.getFilesDir() + "/" + id);
        if (!dir.exists()) {
            Log.e("Batiment", "load: Impossible de trouver le dossier " + dir.getAbsolutePath());
            return null;
        }

        File file = new File(dir.getAbsolutePath(), "data.ser");
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
