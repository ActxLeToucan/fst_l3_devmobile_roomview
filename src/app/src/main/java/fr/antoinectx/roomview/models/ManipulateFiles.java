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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class ManipulateFiles {
    /**
     * Delete a file or a directory recursively
     *
     * @param fileOrDirectory The file or directory to delete
     * @param logTag          The log tag
     */
    protected static void deleteRecursive(@NonNull File fileOrDirectory, String logTag) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                deleteRecursive(child, logTag);
            }
        }
        if (!fileOrDirectory.delete()) {
            Log.e(logTag, "deleteRecursive: Can not delete " + fileOrDirectory.getAbsolutePath());
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
    private static void zipElement(@NonNull File fileOrDirectory, String fileName, @NonNull ZipOutputStream zipOut) throws IOException {
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
     * @param fileOrDirectory      The file or directory to zip
     * @param fos                  The file output stream of the zip file
     * @param includeRootDirectory If the root directory should be included in the zip file
     * @return True if the zip is successful, false otherwise
     * @throws IOException If an error occurs while manipulating the files
     * @see #zipElement(File, String, ZipOutputStream)
     * @see #zip(File, ParcelFileDescriptor, boolean)
     * @see #zip(File, File, boolean)
     */
    private static boolean zip(@NonNull File fileOrDirectory, @NonNull FileOutputStream fos, boolean includeRootDirectory) throws IOException {
        if (!fileOrDirectory.exists()) {
            throw new IOException(fileOrDirectory.getAbsolutePath() + " does not exist");
        }
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        if (fileOrDirectory.isDirectory() && !includeRootDirectory) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                zipElement(child, child.getName(), zipOut);
            }
        } else {
            zipElement(fileOrDirectory, fileOrDirectory.getName(), zipOut);
        }
        zipOut.close();
        fos.close();
        return true;
    }

    /**
     * Zip a file or a directory to a new file
     *
     * @param fileOrDirectory      The file or directory to zip
     * @param zipFile              The zip file to create
     * @param includeRootDirectory If the root directory should be included in the zip file
     * @return True if the zip is successful, false otherwise
     * @throws IOException If an error occurs while manipulating the files
     * @see #zip(File, FileOutputStream, boolean)
     * @see #zip(File, ParcelFileDescriptor, boolean)
     */
    protected static boolean zip(@NonNull File fileOrDirectory, @NonNull File zipFile, boolean includeRootDirectory) throws IOException {
        if (zipFile.exists()) {
            throw new IOException(zipFile.getAbsolutePath() + " already exists");
        }
        return zip(fileOrDirectory, new FileOutputStream(zipFile), includeRootDirectory);
    }

    /**
     * Zip a file or a directory
     *
     * @param fileOrDirectory      The file or directory to zip
     * @param parcelFileDescriptor The parcel file descriptor of the zip file to create
     * @param includeRootDirectory If the root directory should be included in the zip file
     * @return True if the zip is successful, false otherwise
     * @throws IOException If an error occurs while manipulating the files
     * @see #zip(File, FileOutputStream, boolean)
     * @see #zip(File, File, boolean)
     */
    protected static boolean zip(@NonNull File fileOrDirectory, @NonNull ParcelFileDescriptor parcelFileDescriptor, boolean includeRootDirectory) throws IOException {
        return zip(fileOrDirectory, new FileOutputStream(parcelFileDescriptor.getFileDescriptor()), includeRootDirectory);
    }

    private static void unzip(@NonNull FileInputStream fis, @NonNull File destination) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destination, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    protected static void unzip(@NonNull ParcelFileDescriptor parcelFileDescriptor, @NonNull File destination) throws IOException {
        unzip(new FileInputStream(parcelFileDescriptor.getFileDescriptor()), destination);
    }

    @NonNull
    private static File newFile(File destinationDir, @NonNull ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
