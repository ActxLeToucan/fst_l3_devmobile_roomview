package fr.antoinectx.roomview;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.Building;

public class AreaActivity extends MyActivity {
    private Building building;
    private Area area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area);

        building = Building.fromJSONString(getIntent().getStringExtra("building"));
        area = Area.fromJSONString(getIntent().getStringExtra("area"));
        if (building == null || area == null) {
            finish();
            return;
        }

        initAppBar(area.getName(), "TODO : orientation courante", true);

        ImageView imageView = findViewById(R.id.areaActivity_imageView);
        ImageButton buttonLeft = findViewById(R.id.areaActivity_buttonLeft);
        ImageButton buttonRight = findViewById(R.id.areaActivity_buttonRight);
        // todo enum orientation
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_area, menu);
        return true;
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
}