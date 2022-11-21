package fr.antoinectx.roomview;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.Building;
import fr.antoinectx.roomview.models.Direction;

public class PassagesActivity extends MyActivity {
    private Building building;
    private Area area;
    private Direction direction;
    private int previousNumberOfPointers = 0;
    private double[] previousSelection = new double[4];

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passages);

        building = Building.fromJSONString(getIntent().getStringExtra("building"));
        area = Area.fromJSONString(getIntent().getStringExtra("area"));
        direction = Direction.valueOf(getIntent().getStringExtra("direction"));
        if (building == null || area == null) {
            finish();
            return;
        }

        initAppBar(direction.getName(this), "", true);

        SurfaceView surfaceView = findViewById(R.id.passagesActivity_surfaceView);
        surfaceView.setZOrderOnTop(true);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        ImageView imageView = findViewById(R.id.passagesActivity_imageView);
        File photo = area.getFile(this, direction);
        if (photo == null || !photo.exists()) {
            Toast.makeText(this, R.string.no_photo, Toast.LENGTH_SHORT).show();
            finish();
        }
        Glide.with(this)
                .load(photo)
                .into(imageView);

        imageView.setOnTouchListener((view, motionEvent) -> {
            int imageWidth = imageView.getDrawable().getIntrinsicWidth();
            int imageHeight = imageView.getDrawable().getIntrinsicHeight();
            int viewWidth = imageView.getWidth();
            int viewHeight = imageView.getHeight();

            int deltaWidth = imageWidth - viewWidth;
            int deltaHeight = imageHeight - viewHeight;

            int nbPointers = motionEvent.getPointerCount();

            if (nbPointers == 2) {
                // selection sur la photo
                double x1 = motionEvent.getX(0) + deltaWidth / 2.0;
                double y1 = motionEvent.getY(0) + deltaHeight / 2.0;
                double x2 = motionEvent.getX(1) + deltaWidth / 2.0;
                double y2 = motionEvent.getY(1) + deltaHeight / 2.0;

                // selection relative sur la photo
                double x1Rel = x1 / imageWidth;
                if (x1Rel < 0) x1Rel = 0;
                else if (x1Rel > 1) x1Rel = 1;
                double y1Rel = y1 / imageHeight;
                if (y1Rel < 0) y1Rel = 0;
                else if (y1Rel > 1) y1Rel = 1;
                double x2Rel = x2 / imageWidth;
                if (x2Rel < 0) x2Rel = 0;
                else if (x2Rel > 1) x2Rel = 1;
                double y2Rel = y2 / imageHeight;
                if (y2Rel < 0) y2Rel = 0;
                else if (y2Rel > 1) y2Rel = 1;

                previousSelection[0] = x1Rel;
                previousSelection[1] = y1Rel;
                previousSelection[2] = x2Rel;
                previousSelection[3] = y2Rel;

                boolean isValidSelection = !(x1Rel == x2Rel || y1Rel == y2Rel);

                // selection sur l'ecran
                Rect rect = new Rect();
                rect.left = (int) motionEvent.getX(0);
                rect.top = (int) motionEvent.getY(0);
                rect.right = (int) motionEvent.getX(1);
                rect.bottom = (int) motionEvent.getY(1);
                rect.sort();

                Paint paint = new Paint();
                paint.setColor(isValidSelection ? Color.GREEN : Color.RED);
                paint.setAlpha(100);
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
                canvas.drawRect(rect, paint);
                surfaceHolder.unlockCanvasAndPost(canvas);
            } else if (nbPointers < 2 && previousNumberOfPointers == 2) {
                boolean isValidSelection = !(previousSelection[0] == previousSelection[2] || previousSelection[1] == previousSelection[3]);

                if (isValidSelection) {
                    // TODO
                } else {
                    // TODO
                }

                previousSelection = new double[4];
            } else {
                previousSelection = new double[4];
            }

            previousNumberOfPointers = nbPointers;

            return true;
        });
    }
}