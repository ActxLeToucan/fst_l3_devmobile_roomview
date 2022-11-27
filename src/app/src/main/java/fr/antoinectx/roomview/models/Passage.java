package fr.antoinectx.roomview.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class Passage {
    private final String id;
    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;
    @NonNull
    private String autreCoteId;

    private Passage(String id, double x1, double y1, double x2, double y2, @NonNull String autreCoteId) {
        this.id = id;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.autreCoteId = autreCoteId;
    }

    public Passage(double x1, double y1, double x2, double y2, @NonNull Area autreCote) {
        this.id = UUID.randomUUID().toString();
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.autreCoteId = autreCote.getId();
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
                    json.getString("autreCote")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getId() {
        return id;
    }

    @NonNull
    public String getAutreCoteId() {
        return autreCoteId;
    }

    @Nullable
    public Area getAutreCote(@NonNull List<Area> areas) {
        for (Area area : areas) {
            if (area.getId().equals(autreCoteId)) {
                return area;
            }
        }
        return null;
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

    public void setAutreCote(@NonNull Area autreCote) {
        this.autreCoteId = autreCote.getId();
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
            json.put("autreCote", autreCoteId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
}
