package fr.antoinectx.roomview.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class Passage {
    /**
     * The passage unique ID
     */
    private final String id;
    /**
     * X coordinate of the passage's first point
     */
    private final double x1;
    /**
     * Y coordinate of the passage's first point
     */
    private final double y1;
    /**
     * X coordinate of the passage's second point
     */
    private final double x2;
    /**
     * Y coordinate of the passage's second point
     */
    private final double y2;
    /**
     * Area unique ID of the passage's destination
     */
    @NonNull
    private String otherSideId;

    /**
     * Complete constructor, only used when loading a passage from JSON
     *
     * @param id          The passage unique ID
     * @param x1          X coordinate of the passage's first point
     * @param y1          Y coordinate of the passage's first point
     * @param x2          X coordinate of the passage's second point
     * @param y2          Y coordinate of the passage's second point
     * @param otherSideId Area unique ID of the passage's destination
     */
    private Passage(String id, double x1, double y1, double x2, double y2, @NonNull String otherSideId) {
        this.id = id;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.otherSideId = otherSideId;
    }

    /**
     * Default constructor
     *
     * @param x1        X coordinate of the passage's first point
     * @param y1        Y coordinate of the passage's first point
     * @param x2        X coordinate of the passage's second point
     * @param y2        Y coordinate of the passage's second point
     * @param otherSide Passage's destination
     */
    public Passage(double x1, double y1, double x2, double y2, @NonNull Area otherSide) {
        this.id = UUID.randomUUID().toString();
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.otherSideId = otherSide.getId();
    }

    /**
     * Convert a JSON object to a Passage object
     *
     * @param json The JSON object
     * @return The Passage object
     */
    @Nullable
    public static Passage fromJSON(JSONObject json) {
        try {
            return new Passage(
                    json.getString("id"),
                    json.getDouble("x1"),
                    json.getDouble("y1"),
                    json.getDouble("x2"),
                    json.getDouble("y2"),
                    json.getString("otherSide")
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
    public String getOtherSideId() {
        return otherSideId;
    }

    @Nullable
    public Area getOtherSide(@NonNull List<Area> areas) {
        for (Area area : areas) {
            if (area.getId().equals(otherSideId)) {
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

    /**
     * Whether the passage contains the given point
     *
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @return True if the passage contains the point, false otherwise
     */
    public boolean contains(double x, double y) {
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public void setOtherSide(@NonNull Area otherSide) {
        this.otherSideId = otherSide.getId();
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
            json.put("otherSide", otherSideId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
}
