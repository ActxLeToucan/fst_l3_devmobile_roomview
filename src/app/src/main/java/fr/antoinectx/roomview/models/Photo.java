package fr.antoinectx.roomview.models;

import java.util.ArrayList;
import java.util.List;

public class Photo {
    private final List<Passage> passages;

    public Photo() {
        passages = new ArrayList<>();
    }

    public List<Passage> getPassages() {
        return passages;
    }
}
