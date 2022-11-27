package fr.antoinectx.roomview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.Building;
import fr.antoinectx.roomview.models.Direction;
import fr.antoinectx.roomview.models.Passage;

/**
 * Listens to touch events on the image view
 */
interface OnTouchListner {
    /**
     * Called when the user selects an area on the image
     *
     * @param surfaceHolder   The surface holder to draw on
     * @param parent          Parent activity if you need the context
     * @param imageSelection  The selection on the image (relative position)
     * @param screenSelection The selection on the screen (absolute position)
     */
    void onSelection(SurfaceHolder surfaceHolder, Context parent, double[] imageSelection, double[] screenSelection);

    /**
     * Called when the user cancels the selection
     *
     * @param surfaceHolder  The surface holder to draw on
     * @param parent         Parent activity if you need the context
     * @param imageSelection The selection on the image (relative position)
     * @param clickEnabler   Must be called to re-enable the passage click listener
     */
    void afterSelection(SurfaceHolder surfaceHolder, Context parent, double[] imageSelection, SimpleClickEnabler clickEnabler);

    /**
     * Called when the user clicks on a passage
     *
     * @param surfaceHolder The surface holder to draw on
     * @param parent        Parent activity if you need the context
     * @param passage       The passage that was clicked
     */
    void onPassageClick(SurfaceHolder surfaceHolder, Context parent, Passage passage);
}

/**
 * Re-enables the passage click listener
 */
interface SimpleClickEnabler {
    /**
     * Re-enables the passage click listener
     */
    void enable();
}


public abstract class PassageViewActivity extends MyActivity {
    protected Building building;
    protected Area area;
    protected Direction direction;
    protected ImageView imageView;
    private int previousNumberOfPointers = 0;
    private double[] previousSelection = new double[4];
    private boolean allowSimpleClick = true;
    private Drawable resource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        building = Building.fromJSONString(getIntent().getStringExtra("building"));
        area = Area.fromJSONString(getIntent().getStringExtra("area"));
        direction = Direction.valueOf(getIntent().getStringExtra("direction"));
        if (building == null || area == null) {
            finish();
        }
    }

    /**
     * Draw the passages on the surface
     *
     * @param surfaceHolder The surface holder to draw on
     * @see #draw(SurfaceHolder, List)
     * @see #draw(SurfaceHolder, List, int)
     */
    protected void draw(@NonNull SurfaceHolder surfaceHolder) {
        List<Passage> passages = area.getDirectionPhoto(direction) == null
                ? null
                : area.getDirectionPhoto(direction).getPassages();
        draw(surfaceHolder, passages);
    }

    /**
     * Draw a list of passages on the surface with the default color
     *
     * @param surfaceHolder The surface holder to draw on
     * @param passages      The passages to draw
     * @see #draw(SurfaceHolder)
     * @see #draw(SurfaceHolder, List, int)
     */
    protected void draw(@NonNull SurfaceHolder surfaceHolder, @Nullable List<Passage> passages) {
        draw(surfaceHolder, passages, Color.BLUE);
    }

    /**
     * Draw a list of passages on the surface with a specific color
     *
     * @param surfaceHolder The surface holder to draw on
     * @param passages      The passages to draw
     * @param color         The color to draw the passages
     * @see #draw(SurfaceHolder)
     * @see #draw(SurfaceHolder, List)
     */
    protected void draw(@NonNull SurfaceHolder surfaceHolder, @Nullable List<Passage> passages, int color) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            drawPassages(canvas, passages, color);
            surfaceHolder.unlockCanvasAndPost(canvas);
        } else {
            Log.e("PassagesActivity", "Cannot draw canvas");
        }
    }

    /**
     * Draw a list of passages on the canvas with a specific color on a canvas
     *
     * @param canvas   The canvas to draw on
     * @param passages The passages to draw
     * @param color    The color to draw the passages
     */
    private void drawPassages(@NonNull Canvas canvas, @Nullable List<Passage> passages, int color) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (resource == null || passages == null) {
            return;
        }

        int imageWidth = resource.getIntrinsicWidth();
        int imageHeight = resource.getIntrinsicHeight();
        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();

        int deltaWidth = imageWidth - viewWidth;
        int deltaHeight = imageHeight - viewHeight;

        for (Passage passage : passages) {
            Rect rect = new Rect();
            rect.left = (int) (passage.getX1() * imageWidth - deltaWidth / 2.0);
            rect.top = (int) (passage.getY1() * imageHeight - deltaHeight / 2.0);
            rect.right = (int) (passage.getX2() * imageWidth - deltaWidth / 2.0);
            rect.bottom = (int) (passage.getY2() * imageHeight - deltaHeight / 2.0);
            rect.sort();

            Paint paint = new Paint();
            paint.setColor(color);
            paint.setAlpha(100);
            canvas.drawRect(rect, paint);
        }
    }

    /**
     * Set the image in the image view and let you define what to do on some events
     *
     * @param file           The file to display
     * @param onTouchListner The listener to call on the events
     */
    protected void applyImage(File file, OnTouchListner onTouchListner) {
        imageView.setOnTouchListener(null);
        resource = null;

        Glide.with(this)
                .load(file)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        initSurface(null, null);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        initSurface(onTouchListner, resource);
                        return false;
                    }
                })
                .into(imageView);
    }

    /**
     * Initialize the surface view to draw the passages
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initSurface(@Nullable OnTouchListner onTouchListner, @Nullable Drawable resource) {
        this.resource = resource;

        SurfaceView surfaceView = findViewById(R.id.passagesView_surfaceView);
        surfaceView.setVisibility(View.GONE); // to force the surface view to be created
        surfaceView.setZOrderOnTop(true);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                draw(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                draw(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        surfaceView.setVisibility(View.VISIBLE);


        if (onTouchListner == null) {
            return;
        }


        imageView.setOnTouchListener((v, motionEvent) -> {
            int imageWidth = this.resource.getIntrinsicWidth();
            int imageHeight = this.resource.getIntrinsicHeight();
            int viewWidth = imageView.getWidth();
            int viewHeight = imageView.getHeight();

            int deltaWidth = imageWidth - viewWidth;
            int deltaHeight = imageHeight - viewHeight;

            int nbPointers = motionEvent.getPointerCount();

            if (nbPointers == 2) { // Selection passage
                allowSimpleClick = false;

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

                onTouchListner.onSelection(
                        surfaceHolder,
                        this,
                        new double[]{x1Rel, y1Rel, x2Rel, y2Rel},
                        new double[]{
                                motionEvent.getX(0),
                                motionEvent.getY(0),
                                motionEvent.getX(1),
                                motionEvent.getY(1)
                        });
            } else {
                if (nbPointers < 2 && previousNumberOfPointers == 2) {
                    onTouchListner.afterSelection(surfaceHolder, this, previousSelection, () -> allowSimpleClick = true);
                } else if (nbPointers == 1 && motionEvent.getAction() == MotionEvent.ACTION_UP && allowSimpleClick) {
                    // Click sur un passage
                    double x = motionEvent.getX() + deltaWidth / 2.0;
                    double y = motionEvent.getY() + deltaHeight / 2.0;

                    double xRel = x / imageWidth;
                    if (xRel < 0) xRel = 0;
                    else if (xRel > 1) xRel = 1;
                    double yRel = y / imageHeight;
                    if (yRel < 0) yRel = 0;
                    else if (yRel > 1) yRel = 1;

                    double finalXRel = xRel;
                    double finalYRel = yRel;

                    List<Passage> passages = area.getDirectionPhoto(direction).getPassages().stream()
                            .filter(p -> p.contains(finalXRel, finalYRel))
                            .collect(Collectors.toList());

                    if (passages.size() > 1) {
                        draw(surfaceHolder, passages);
                        CharSequence[] items = passages.stream()
                                .map(p -> p.getAutreCote(building.getAreas()).getName())
                                .toArray(CharSequence[]::new);
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.passagesView_selectPassage_title)
                                .setItems(items, (dialog, item) -> {
                                    onTouchListner.onPassageClick(surfaceHolder, this, passages.get(item));
                                })
                                .setOnCancelListener(dialogInterface -> draw(surfaceHolder))
                                .show();
                    } else if (passages.size() == 1) {
                        onTouchListner.onPassageClick(surfaceHolder, this, passages.get(0));
                    } else {
                        draw(surfaceHolder);
                    }
                }

                previousSelection = new double[4];
            }

            previousNumberOfPointers = nbPointers;

            return true;
        });
    }
}