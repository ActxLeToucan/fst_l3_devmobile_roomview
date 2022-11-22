package fr.antoinectx.roomview.models;

import androidx.annotation.Nullable;

import org.json.JSONObject;

public class Passage {
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

    private Passage(double x1, double y1, double x2, double y2, @Nullable String autreCoteId) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.autreCote = null;
        this.autreCoteId = autreCoteId;
    }

    public Passage(double x1, double y1, double x2, double y2, @Nullable Area autreCote) {
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

    @Nullable
    public Area getAutreCote() {
        return autreCote;
    }

    public void setAutreCote(@Nullable Area autreCote) {
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
