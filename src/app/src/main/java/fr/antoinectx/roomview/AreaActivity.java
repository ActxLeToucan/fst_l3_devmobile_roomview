package fr.antoinectx.roomview;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.Building;
import fr.antoinectx.roomview.models.Direction;

public class AreaActivity extends MyActivity {
    private Building building;
    private Area area;
    private Direction direction;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area);

        building = Building.fromJSONString(getIntent().getStringExtra("building"));
        area = Area.fromJSONString(getIntent().getStringExtra("area"));
        direction = Direction.valueOf(getIntent().getStringExtra("direction"));
        if (building == null || area == null) {
            finish();
            return;
        }

        initAppBar(area.getName(), direction.getName(this), true);

        imageView = findViewById(R.id.areaActivity_imageView);
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
                    building.save(this);
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    public void passages(MenuItem item) {
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
        Glide.with(this)
                .load(area.getFile(this, direction))
                .into(imageView);

        invalidateOptionsMenu();
    }
}