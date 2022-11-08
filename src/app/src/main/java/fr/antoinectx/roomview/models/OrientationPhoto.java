package fr.antoinectx.roomview.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrientationPhoto {
    private final List<Passage> passages;
    private final String filename;

    private OrientationPhoto(List<Passage> passages, String filename) {
        this.passages = passages;
        this.filename = filename;
    }

    public OrientationPhoto(String filename) {
        this.passages = new ArrayList<>();
        this.filename = filename;
    }

    public List<Passage> getPassages() {
        return passages;
    }

    /**
     * Convert the object to a JSON object
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

    /**
     * Convert a JSON object to an OrientationPhoto object
     * @param json The JSON object
     * @return The OrientationPhoto object
     */
    public static OrientationPhoto fromJSON(JSONObject json) {
        try {
            List<Passage> passages = new ArrayList<>();
            for (int i = 0; i < json.getJSONArray("passages").length(); i++) {
                passages.add(Passage.fromJSON(json.getJSONArray("passages").getJSONObject(i)));
            }

            return new OrientationPhoto(
                    passages,
                    json.getString("filename")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}

