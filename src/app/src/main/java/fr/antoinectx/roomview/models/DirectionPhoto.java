package fr.antoinectx.roomview.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DirectionPhoto {
    private final List<Passage> passages;
    @NonNull
    private final String filename;

    private DirectionPhoto(List<Passage> passages, @NonNull String filename) {
        this.passages = passages;
        this.filename = filename;
    }

    public DirectionPhoto(@NonNull String filename) {
        this.passages = new ArrayList<>();
        this.filename = filename;
    }

    /**
     * Convert a JSON object to an DirectionPhoto object
     *
     * @param json The JSON object
     * @return The DirectionPhoto object
     */
    @Nullable
    public static DirectionPhoto fromJSON(@NonNull JSONObject json) {
        try {
            List<Passage> passages = new ArrayList<>();
            for (int i = 0; i < json.getJSONArray("passages").length(); i++) {
                passages.add(Passage.fromJSON(json.getJSONArray("passages").getJSONObject(i)));
            }

            return new DirectionPhoto(
                    passages,
                    json.getString("filename")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Passage> getPassages() {
        return passages;
    }

    @NonNull
    public String getFilename() {
        return filename;
    }

    /**
     * Convert the object to a JSON object
     *
     * @return The JSON object
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            JSONArray passages = new JSONArray();
            for (Passage passage : this.passages) {
                passages.put(passage.toJSON());
            }
            json.put("passages", passages);
            json.put("filename", filename);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}

