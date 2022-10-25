package fr.antoinectx.roomview.models;

import androidx.annotation.Nullable;

public class Passage {
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    @Nullable
    private Zone autreCote;

    public Passage(int x1, int y1, int x2, int y2, @Nullable Zone autreCote) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.autreCote = autreCote;
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
}
