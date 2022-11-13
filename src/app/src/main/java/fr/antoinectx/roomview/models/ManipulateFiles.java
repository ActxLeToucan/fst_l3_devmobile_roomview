package fr.antoinectx.roomview.models;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;

public abstract class ManipulateFiles {
    /**
     * Delete a file or a directory recursively
     *
     * @param fileOrDirectory The file or directory to delete
     */
    protected void deleteRecursive(@NonNull File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                deleteRecursive(child);
            }
        }
        if (!fileOrDirectory.delete()) {
            Log.e(getClass().getSimpleName(), "deleteRecursive: Can not delete " + fileOrDirectory.getAbsolutePath());
        }
    }
}
