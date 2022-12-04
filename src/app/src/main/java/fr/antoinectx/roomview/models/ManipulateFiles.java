package fr.antoinectx.roomview.models;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    /**
     * Add a file to a zip file
     *
     * @param fileOrDirectory The file or directory to add
     * @param fileName        The name of the file in the zip file
     * @param zipOut          The zip file
     * @throws IOException If an error occurs while manipulating the files
     */
    private void zipElement(@NonNull File fileOrDirectory, String fileName, @NonNull ZipOutputStream zipOut) throws IOException {
        if (fileOrDirectory.isHidden()) {
            return;
        }
        if (fileOrDirectory.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipElement(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileOrDirectory);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    /**
     * Zip a file or a directory
     *
     * @param fileOrDirectory The file or directory to zip
     * @param fos             The file output stream of the zip file
     * @return True if the zip is successful, false otherwise
     * @throws IOException If an error occurs while manipulating the files
     * @see #zipElement(File, String, ZipOutputStream)
     * @see #zip(File, ParcelFileDescriptor)
     * @see #zip(File, File)
     */
    private boolean zip(@NonNull File fileOrDirectory, @NonNull FileOutputStream fos) throws IOException {
        if (!fileOrDirectory.exists()) {
            Log.e(getClass().getSimpleName(), "zip: " + fileOrDirectory.getAbsolutePath() + " does not exist");
            return false;
        }
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipElement(fileOrDirectory, fileOrDirectory.getName(), zipOut);
        zipOut.close();
        fos.close();
        return true;
    }

    /**
     * Zip a file or a directory to a new file
     *
     * @param fileOrDirectory The file or directory to zip
     * @param zipFile         The zip file to create
     * @return True if the zip is successful, false otherwise
     * @throws IOException If an error occurs while manipulating the files
     * @see #zip(File, FileOutputStream)
     * @see #zip(File, ParcelFileDescriptor)
     */
    protected boolean zip(@NonNull File fileOrDirectory, @NonNull File zipFile) throws IOException {
        if (zipFile.exists()) {
            Log.e(getClass().getSimpleName(), "zip: " + zipFile.getAbsolutePath() + " already exists");
            return false;
        }
        return zip(fileOrDirectory, new FileOutputStream(zipFile));
    }

    /**
     * Zip a file or a directory
     *
     * @param fileOrDirectory      The file or directory to zip
     * @param parcelFileDescriptor The parcel file descriptor of the zip file to create
     * @return True if the zip is successful, false otherwise
     * @throws IOException If an error occurs while manipulating the files
     * @see #zip(File, FileOutputStream)
     * @see #zip(File, File)
     */
    protected boolean zip(@NonNull File fileOrDirectory, @NonNull ParcelFileDescriptor parcelFileDescriptor) throws IOException {
        return zip(fileOrDirectory, new FileOutputStream(parcelFileDescriptor.getFileDescriptor()));
    }
}
