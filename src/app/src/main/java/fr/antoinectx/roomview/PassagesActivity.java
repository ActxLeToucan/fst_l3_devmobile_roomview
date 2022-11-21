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
                // position sur l'image
                Rect rect = new Rect();
                rect.left = (int) motionEvent.getX(0) + deltaWidth / 2;
                rect.top = (int) motionEvent.getY(0) + deltaHeight / 2;
                rect.right = (int) motionEvent.getX(1) + deltaWidth / 2;
                rect.bottom = (int) motionEvent.getY(1) + deltaHeight / 2;
                rect.sort();

                // position sur l'imageView
                Rect rect2 = new Rect();
                rect2.left = (int) motionEvent.getX(0);
                rect2.top = (int) motionEvent.getY(0);
                rect2.right = (int) motionEvent.getX(1);
                rect2.bottom = (int) motionEvent.getY(1);
                rect2.sort();

                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setAlpha(100);
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
                canvas.drawRect(rect, paint);
                paint.setColor(Color.BLUE);
                paint.setAlpha(100);
                canvas.drawRect(rect2, paint);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }

            return true;
        });
    }
}