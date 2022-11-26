package fr.antoinectx.roomview.models;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.UUID;

public class Passage {
    private String id;
    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;
    @Nullable
    private Area autreCote;
    /**
     * Used when loading a passage from a file
     */
    @Nullable
    private String autreCoteId;

    private Passage(String id, double x1, double y1, double x2, double y2, @Nullable String autreCoteId) {
        this.id = id;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.autreCote = null;
        this.autreCoteId = autreCoteId;
    }

    public Passage(double x1, double y1, double x2, double y2, @Nullable Area autreCote) {
        this.id = UUID.randomUUID().toString();
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.autreCote = autreCote;
    }

    /**
     * Convert a JSON object to a Passage object
     *
     * @param json The JSON object
     * @return The Passage object
     */
    public static Passage fromJSON(JSONObject json) {
        try {
            return new Passage(
                    json.getString("id"),
                    json.getDouble("x1"),
                    json.getDouble("y1"),
                    json.getDouble("x2"),
                    json.getDouble("y2"),
                    !json.isNull("autreCote") ? json.getString("autreCote") : null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getAutreCoteId() {
        return autreCote != null ? autreCote.getId() : autreCoteId;
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public boolean contains(double x, double y) {
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    @Nullable
    public Area getAutreCote() {
        return autreCote;
    }

    public void setAutreCote(Area autreCote) {
        this.autreCote = autreCote;
        this.autreCoteId = null;
    }

    public boolean estValide() {
        return autreCote != null;
    }

    /**
     * Convert the object to a JSON object
     *
     * @return The JSON object
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("x1", x1);
            json.put("y1", y1);
            json.put("x2", x2);
            json.put("y2", y2);
            json.put("autreCote", autreCote == null ? JSONObject.NULL : autreCote.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
}
