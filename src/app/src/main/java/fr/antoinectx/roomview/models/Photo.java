package fr.antoinectx.roomview.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Passage> passages;

    public Photo() {
        passages = new ArrayList<>();
    }

    public List<Passage> getPassages() {
        return passages;
    }
}
