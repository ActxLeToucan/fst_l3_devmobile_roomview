package fr.antoinectx.roomview.models;

import android.content.Context;

import fr.antoinectx.roomview.R;

public enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    /**
     * Get the name of the direction in the current language
     *
     * @param context The context of the application
     *                (use getApplicationContext() or getBaseContext() or this in an activity),
     * @return The name of the direction
     */
    public String getName(Context context) {
        switch (this) {
            case NORTH:
                return context.getString(R.string.north);
            case EAST:
                return context.getString(R.string.east);
            case SOUTH:
                return context.getString(R.string.south);
            case WEST:
                return context.getString(R.string.west);
            default:
                return "undefined";
        }
    }

    /**
     * Get the next direction in the trigonometric direction (turn left, counter-clockwise)
     * (NORTH -> WEST -> SOUTH -> EAST -> NORTH)
     *
     * @return The next direction
     */
    public Direction getLeft() {
        switch (this) {
            case NORTH:
                return WEST;
            case SOUTH:
                return EAST;
            case WEST:
                return SOUTH;
            case EAST:
            default:
                return NORTH;
        }
    }

    /**
     * Get the previous direction in the trigonometric direction (turn right, clockwise)
     * (NORTH -> EAST -> SOUTH -> WEST -> NORTH)
     *
     * @return The previous direction
     */
    public Direction getRight() {
        switch (this) {
            case NORTH:
                return EAST;
            case EAST:
                return SOUTH;
            case SOUTH:
                return WEST;
            case WEST:
            default:
                return NORTH;
        }
    }
}
