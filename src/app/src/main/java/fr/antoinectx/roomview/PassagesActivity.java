package fr.antoinectx.roomview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.Building;
import fr.antoinectx.roomview.models.Direction;
import fr.antoinectx.roomview.models.DirectionPhoto;
import fr.antoinectx.roomview.models.Passage;

public class PassagesActivity extends MyActivity implements SurfaceHolder.Callback {
    private Building building;
    private Area area;
    private Direction direction;
    private ImageView imageView;
    private int previousNumberOfPointers = 0;
    private double[] previousSelection = new double[4];

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

        imageView = findViewById(R.id.passagesActivity_imageView);
        File photo = area.getFile(this, direction);
        if (photo == null || !photo.exists()) {
            Toast.makeText(this, R.string.no_photo, Toast.LENGTH_SHORT).show();
            finish();
        }
        Glide.with(this)
                .load(photo)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        initSurface();
                        return false;
                    }
                })
                .into(imageView);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSurface() {
        SurfaceView surfaceView = findViewById(R.id.passagesActivity_surfaceView);
        surfaceView.setZOrderOnTop(true);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.addCallback(this);

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
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawRect(rect, paint);
                surfaceHolder.unlockCanvasAndPost(canvas);
            } else if (nbPointers < 2 && previousNumberOfPointers == 2) {
                boolean isValidSelection = !(previousSelection[0] == previousSelection[2] || previousSelection[1] == previousSelection[3]);

                if (isValidSelection) {
                    double x1 = previousSelection[0];
                    double y1 = previousSelection[1];
                    double x2 = previousSelection[2];
                    double y2 = previousSelection[3];

                    List<Area> areas = building.getAreas().stream()
                            .filter(a -> !a.getId().equals(area.getId()))
                            .collect(Collectors.toList());
                    ArrayAdapter<Area> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, areas);


                    View layout = getLayoutInflater().inflate(R.layout.dialog_create_passage, null);
                    MaterialAutoCompleteTextView autoCompleteTextView = layout.findViewById(R.id.passage_field_area);


                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(layout);
                    builder.setOnCancelListener(dialogInterface -> {
                        draw(surfaceHolder);
                    });
                    AlertDialog dialog = builder.create();


                    autoCompleteTextView.setAdapter(adapter);
                    autoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
                        Area selectedArea = (Area) parent.getItemAtPosition(position);
                        if (selectedArea != null) {
                            Passage passage = new Passage(x1, y1, x2, y2, selectedArea);
                            DirectionPhoto directionPhoto = area.getDirectionPhoto(direction);
                            directionPhoto.getPassages().add(passage);
                        } else {
                            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                        }

                        draw(surfaceHolder);

                        dialog.dismiss();
                    });

                    dialog.show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.passagesActivity_invalidSelection_title)
                            .setMessage(R.string.passagesActivity_invalidSelection_message)
                            .setPositiveButton("OK", null)
                            .show();

                    draw(surfaceHolder);
                }

                previousSelection = new double[4];
            } else {
                previousSelection = new double[4];
            }

            previousNumberOfPointers = nbPointers;

            return true;
        });
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        draw(holder);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        draw(holder);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }

    private void draw(@NonNull SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            onDraw(canvas);
            holder.unlockCanvasAndPost(canvas);
        } else {
            Log.e("PassagesActivity", "Cannot draw canvas");
        }
    }

    private void onDraw(@NonNull Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        int imageWidth = imageView.getDrawable().getIntrinsicWidth();
        int imageHeight = imageView.getDrawable().getIntrinsicHeight();
        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();

        int deltaWidth = imageWidth - viewWidth;
        int deltaHeight = imageHeight - viewHeight;

        List<Passage> passages = area.getDirectionPhoto(direction).getPassages();
        for (Passage passage : passages) {
            Rect rect = new Rect();
            rect.left = (int) (passage.getX1() * imageWidth - deltaWidth / 2.0);
            rect.top = (int) (passage.getY1() * imageHeight - deltaHeight / 2.0);
            rect.right = (int) (passage.getX2() * imageWidth - deltaWidth / 2.0);
            rect.bottom = (int) (passage.getY2() * imageHeight - deltaHeight / 2.0);
            rect.sort();

            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setAlpha(100);
            canvas.drawRect(rect, paint);
        }
    }
}