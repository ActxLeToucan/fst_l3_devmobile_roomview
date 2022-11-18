package fr.antoinectx.roomview.models;

import android.content.Context;

import fr.antoinectx.roomview.R;

public enum Orientation {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    /**
     * Get the name of the orientation in the current language
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity),
     * @return The name of the orientation
     */
    public String getName(Context context) {
        if (this == NORTH) {
            return context.getString(R.string.north);
        } else if (this == EAST) {
            return context.getString(R.string.east);
        } else if (this == SOUTH) {
            return context.getString(R.string.south);
        } else if (this == WEST) {
            return context.getString(R.string.west);
        }
        return null;
    }
}
