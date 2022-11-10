package fr.antoinectx.roomview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

public class Tools {
    /**
     * Get the bitmap from a path, and rotate it if needed
     * (from <a href="https://stackoverflow.com/a/14066265">StackOverflow</a>)
     * @param path The path of the image
     * @return The bitmap of the image
     */
    @Nullable
    public static Bitmap getBitmapFromPath(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);

        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        return rotatedBitmap;
    }

    /**
     * Rotate a bitmap according to its EXIF orientation
     * (from <a href="https://stackoverflow.com/a/14066265">StackOverflow</a>)
     * @param source The bitmap to rotate
     * @param angle The angle to rotate the bitmap
     * @return The rotated bitmap
     */
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}
