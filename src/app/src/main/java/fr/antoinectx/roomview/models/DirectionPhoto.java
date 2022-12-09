package fr.antoinectx.roomview.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DirectionPhoto {
    /**
     * Passages to other areas
     */
    private final List<Passage> passages;
    /**
     * The photo file name
     */
    @NonNull
    private final String filename;
    /**
     * The date of the photo
     */
    private final Date date;
    /**
     * The weather when the photo was taken
     */
    @Nullable
    private String weather, temperature, icon;

    /**
     * Complete constructor, only used when loading a direction photo from JSON
     *
     * @param passages Passages to other areas
     * @param filename The photo file name
     * @param date     The date of the photo
     */
    private DirectionPhoto(List<Passage> passages, @NonNull String filename, Date date, @Nullable String weather, @Nullable String temperature, @Nullable String icon) {
        this.passages = passages;
        this.filename = filename;
        this.date = date;
        this.weather = weather;
        this.temperature = temperature;
        this.icon = icon;
    }

    /**
     * Default constructor
     *
     * @param filename The photo file name
     */
    public DirectionPhoto(@NonNull String filename) {
        this.passages = new ArrayList<>();
        this.filename = filename;
        this.date = new Date();
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
                    json.getString("filename"),
                    new Date(json.getLong("date")),
                    !json.isNull("weather") ? json.getString("weather") : null,
                    !json.isNull("temperature") ? json.getString("temperature") : null,
                    !json.isNull("icon") ? json.getString("icon") : null
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

    public Date getDate() {
        return date;
    }

    @Nullable
    public String getWeather() {
        return weather;
    }

    public void setWeather(@Nullable String weather) {
        this.weather = weather;
    }

    @Nullable
    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(@Nullable String temperature) {
        this.temperature = temperature;
    }

    @Nullable
    public String getIcon() {
        return icon;
    }

    public void setIcon(@Nullable String icon) {
        this.icon = icon;
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
            json.put("date", date.getTime());
            json.put("weather", weather == null ? JSONObject.NULL : weather);
            json.put("temperature", temperature == null ? JSONObject.NULL : temperature);
            json.put("icon", icon == null ? JSONObject.NULL : icon);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}

