package fr.antoinectx.roomview;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;

import java.io.File;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.Building;
import fr.antoinectx.roomview.models.Direction;
import fr.antoinectx.roomview.models.DirectionPhoto;

public class EditAreaActivity extends MyActivity {
    private Building building;
    private Area oldArea;
    private Area area;
    private boolean saved = false;
    private ImageButton north;
    private ImageButton east;
    private ImageButton south;
    private ImageButton west;
    final private ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Direction direction = Direction.valueOf(data.getStringExtra("direction"));
                        String path = data.getStringExtra("path");
                        if (path != null && !path.isEmpty()) {
                            File file = new File(path);
                            if (file.exists()) {
                                String timestamp = String.valueOf(System.currentTimeMillis()); // to invalidate cache
                                File newFile = new File(area.getDirectory(this), direction.getName(this) + '_' + timestamp + ".jpg");
                                file.renameTo(newFile);

                                area.setDirectionPhoto(direction, new DirectionPhoto(newFile.getName()));

                                applyPhotos();
                            }
                        } else {
                            new AlertDialog.Builder(this)
                                    .setIcon(R.drawable.ic_baseline_error_24)
                                    .setTitle(R.string.error)
                                    .setMessage(R.string.error_takePhoto)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }
            });
    private boolean newArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_area);

        blockOrientation(); // Block orientation to prevent data loss

        building = Building.fromJSONString(getIntent().getStringExtra("building"));
        if (building == null) {
            finish();
            return;
        }
        String areaString = getIntent().getStringExtra("area");
        if (areaString != null) {
            oldArea = Area.fromJSONString(areaString);
            area = Area.fromJSONString(areaString);
        }
        if (area == null) {
            area = new Area(building.getId(), getString(R.string.area_new));
            newArea = true;
        }

        initAppBar(area.getName(), getString(R.string.pagename_editArea), true, R.drawable.ic_baseline_close_24, R.string.action_cancel);

        EditText areaName = findViewById(R.id.area_field_name);
        areaName.setText(area.getName());

        north = findViewById(R.id.imageButton_north);
        north.setOnClickListener(v -> {
                    Intent intent = new Intent(this, CameraActivity.class);
                    intent.putExtra("direction", Direction.NORTH.toString());
                    takePhotoLauncher.launch(intent);
                }
        );
        east = findViewById(R.id.imageButton_east);
        east.setOnClickListener(v -> {
                    Intent intent = new Intent(this, CameraActivity.class);
                    intent.putExtra("direction", Direction.EAST.toString());
                    takePhotoLauncher.launch(intent);
                }
        );
        south = findViewById(R.id.imageButton_south);
        south.setOnClickListener(v -> {
                    Intent intent = new Intent(this, CameraActivity.class);
                    intent.putExtra("direction", Direction.SOUTH.toString());
                    takePhotoLauncher.launch(intent);
                }
        );
        west = findViewById(R.id.imageButton_west);
        west.setOnClickListener(v -> {
                    Intent intent = new Intent(this, CameraActivity.class);
                    intent.putExtra("direction", Direction.WEST.toString());
                    takePhotoLauncher.launch(intent);
                }
        );

        applyPhotos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_area, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // photos
        if (!saved && oldArea != null) {
            File oldNorth = oldArea.getFile(this, Direction.NORTH);
            File newNorth = area.getFile(this, Direction.NORTH);
            if (oldNorth != null && newNorth != null && !oldNorth.getName().equals(newNorth.getName())) {
                newNorth.delete();
            }
            File oldEast = oldArea.getFile(this, Direction.EAST);
            File newEast = area.getFile(this, Direction.EAST);
            if (oldEast != null && newEast != null && !oldEast.getName().equals(newEast.getName())) {
                newEast.delete();
            }
            File oldSouth = oldArea.getFile(this, Direction.SOUTH);
            File newSouth = area.getFile(this, Direction.SOUTH);
            if (oldSouth != null && newSouth != null && !oldSouth.getName().equals(newSouth.getName())) {
                newSouth.delete();
            }
            File oldWest = oldArea.getFile(this, Direction.WEST);
            File newWest = area.getFile(this, Direction.WEST);
            if (oldWest != null && newWest != null && !oldWest.getName().equals(newWest.getName())) {
                newWest.delete();
            }
        }

        if (newArea) {
            area.delete(this);
        }
    }

    private void applyPhotos() {
        Glide.with(this)
                .load(area.getFile(this, Direction.NORTH))
                .placeholder(R.drawable.ic_baseline_add_a_photo_24).fitCenter()
                .into(north);
        Glide.with(this)
                .load(area.getFile(this, Direction.EAST))
                .placeholder(R.drawable.ic_baseline_add_a_photo_24).fitCenter()
                .into(east);
        Glide.with(this)
                .load(area.getFile(this, Direction.SOUTH))
                .placeholder(R.drawable.ic_baseline_add_a_photo_24).fitCenter()
                .into(south);
        Glide.with(this)
                .load(area.getFile(this, Direction.WEST))
                .placeholder(R.drawable.ic_baseline_add_a_photo_24).fitCenter()
                .into(west);
    }

    public void save(MenuItem item) {
        saved = true;

        // name
        EditText areaName = findViewById(R.id.area_field_name);
        area.setName(areaName.getText().toString());

        // photos
        if (oldArea != null) {
            File oldNorth = oldArea.getFile(this, Direction.NORTH);
            File newNorth = area.getFile(this, Direction.NORTH);
            if (oldNorth != null && newNorth != null && !oldNorth.getName().equals(newNorth.getName())) {
                oldNorth.delete();
            }
            File oldEast = oldArea.getFile(this, Direction.EAST);
            File newEast = area.getFile(this, Direction.EAST);
            if (oldEast != null && newEast != null && !oldEast.getName().equals(newEast.getName())) {
                oldEast.delete();
            }
            File oldSouth = oldArea.getFile(this, Direction.SOUTH);
            File newSouth = area.getFile(this, Direction.SOUTH);
            if (oldSouth != null && newSouth != null && !oldSouth.getName().equals(newSouth.getName())) {
                oldSouth.delete();
            }
            File oldWest = oldArea.getFile(this, Direction.WEST);
            File newWest = area.getFile(this, Direction.WEST);
            if (oldWest != null && newWest != null && !oldWest.getName().equals(newWest.getName())) {
                oldWest.delete();
            }
        }

        // find old area and replace it
        boolean found = false;
        for (int i = 0; i < building.getAreas().size(); i++) {
            if (building.getAreas().get(i).getId().equals(area.getId())) {
                building.getAreas().set(i, area);
                found = true;
                break;
            }
        }
        if (!found) building.getAreas().add(area);
        building.save(this);
        newArea = false;
        finish();
    }
}