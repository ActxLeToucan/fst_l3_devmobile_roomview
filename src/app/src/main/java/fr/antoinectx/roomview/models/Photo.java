package fr.antoinectx.roomview.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Photo {
    private final List<Passage> passages;

    private Photo(List<Passage> passages) {
        this.passages = passages;
    }

    public Photo() {
        passages = new ArrayList<>();
    }

    public List<Passage> getPassages() {
        return passages;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            JSONArray passages = new JSONArray();
            for (Passage passage : this.passages) {
                passages.put(passage.toJSON());
            }
            json.put("passages", passages);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static Photo fromJSON(JSONObject json) {
        try {
            List<Passage> passages = new ArrayList<>();
            for (int i = 0; i < json.getJSONArray("passages").length(); i++) {
                passages.add(Passage.fromJSON(json.getJSONArray("passages").getJSONObject(i)));
            }
            return new Photo(passages);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
