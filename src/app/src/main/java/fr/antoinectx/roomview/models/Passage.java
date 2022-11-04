package fr.antoinectx.roomview.models;

import androidx.annotation.Nullable;

import org.json.JSONObject;

public class Passage {
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    @Nullable
    private Zone autreCote;
    /**
     * Used when loading a passage from a file
     */
    private String autreCoteId;

    private Passage(int x1, int y1, int x2, int y2, String autreCoteId) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.autreCote = null;
        this.autreCoteId = autreCoteId;
    }

    public Passage(int x1, int y1, int x2, int y2, @Nullable Zone autreCote) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.autreCote = autreCote;
    }

    public String getAutreCoteId() {
        return autreCote == null ? autreCoteId : autreCote.getId();
    }

    public int[] getCoordonnees() {
        return new int[] {x1, y1, x2, y2};
    }

    @Nullable
    public Zone getAutreCote() {
        return autreCote;
    }

    public void setAutreCote(@Nullable Zone autreCote) {
        this.autreCote = autreCote;
    }

    public boolean estValide() {
        return autreCote != null;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("x1", x1);
            json.put("y1", y1);
            json.put("x2", x2);
            json.put("y2", y2);
            if (autreCote != null) {
                json.put("autreCote", autreCote.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static Passage fromJSON(JSONObject json) {
        try {
            return new Passage(
                    json.getInt("x1"),
                    json.getInt("y1"),
                    json.getInt("x2"),
                    json.getInt("y2"),
                    json.optString("autreCote")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
