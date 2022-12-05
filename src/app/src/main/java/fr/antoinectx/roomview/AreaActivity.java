package fr.antoinectx.roomview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Arrays;

import fr.antoinectx.roomview.models.Passage;

public class AreaActivity extends PassageViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area);

        initAppBar(area.getName(), direction.getName(this), true);

        imageView = findViewById(R.id.passagesView_imageView);
        ImageButton buttonLeft = findViewById(R.id.areaActivity_buttonLeft);
        buttonLeft.setOnClickListener(v -> {
            direction = direction.getLeft();
            updateDirection();
        });
        ImageButton buttonRight = findViewById(R.id.areaActivity_buttonRight);
        buttonRight.setOnClickListener(v -> {
            direction = direction.getRight();
            updateDirection();
        });

        updateDirection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_area, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem passagesItem = menu.findItem(R.id.menu_area_passages);
        File photo = area.getFile(this, direction);
        passagesItem.setVisible(photo != null && photo.exists());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!building.reload(this)) {
            finish();
        }
        if (!area.reloadFromBuilding(building)) {
            finish();
        }
        update();
    }

    public void editArea(MenuItem item) {
        Intent intent = new Intent(this, EditAreaActivity.class);
        intent.putExtra("building", building.toJSON().toString());
        intent.putExtra("area", area.toJSON().toString());
        startActivity(intent);
    }

    public void deleteArea(MenuItem item) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setTitle(R.string.warning)
                .setMessage(R.string.warning_deleteArea)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    area.delete(this);
                    building.getAreas().removeIf(a -> a.getId().equals(area.getId()));
                    building.getAreas().forEach(a -> Arrays.asList(a.getDirectionPhotos()).forEach(p -> {
                        if (p != null && p.getPassages() != null) {
                            p.getPassages().removeIf(passage -> passage.getOtherSideId().equals(area.getId()));
                        }
                    }));
                    building.save(this);
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    public void editPassages(MenuItem item) {
        Intent intent = new Intent(this, PassagesActivity.class);
        intent.putExtra("building", building.toJSON().toString());
        intent.putExtra("area", area.toJSON().toString());
        intent.putExtra("direction", direction.name());
        startActivity(intent);
    }

    public void update() {
        toolbar.setTitle(area.getName());
        updateDirection();
    }

    private void updateDirection() {
        toolbar.setSubtitle(direction.getName(this));
        applyImage(area.getFile(this, direction), new OnTouchListner() {
            @Override
            public void onSelection(SurfaceHolder surfaceHolder, Context parent, double[] imageSelection, double[] screenSelection) {
            }

            @Override
            public void afterSelection(SurfaceHolder surfaceHolder, Context parent, double[] imageSelection, SimpleClickEnabler clickEnabler) {
                clickEnabler.enable();
            }

            @Override
            public void onPassageClick(SurfaceHolder surfaceHolder, Context parent, Passage passage) {
                area = passage.getOtherSide(building.getAreas());
                update();
            }
        });

        invalidateOptionsMenu();

        // preload next images
        Glide.with(this)
                .load(area.getFile(this, direction.getLeft()))
                .preload();
        Glide.with(this)
                .load(area.getFile(this, direction.getRight()))
                .preload();

    }
}