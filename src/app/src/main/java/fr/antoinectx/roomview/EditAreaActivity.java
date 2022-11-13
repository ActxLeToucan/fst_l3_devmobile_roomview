package fr.antoinectx.roomview;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.Building;

public class EditAreaActivity extends MyActivity {
    private Building building;
    private Area area;
    private boolean newArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_area);

        building = Building.fromJSONString(getIntent().getStringExtra("building"));
        if (building == null) {
            finish();
            return;
        }
        String areaString = getIntent().getStringExtra("area");
        if (areaString != null) area = Area.fromJSONString(areaString);
        if (area == null) {
            area = new Area(building.getId(), getString(R.string.area_new));
            newArea = true;
        }

        initAppBar(area.getName(), getString(R.string.pagename_editArea), true, R.drawable.ic_baseline_close_24, R.string.action_cancel);

        EditText areaName = findViewById(R.id.area_field_name);
        areaName.setText(area.getName());

        ImageButton north = findViewById(R.id.imageButton_north);
        Glide.with(this)
                .load(area.getNorthFile(this))
                .placeholder(R.drawable.ic_baseline_add_a_photo_24).fitCenter()
                .into(north);
        ImageButton east = findViewById(R.id.imageButton_east);
        Glide.with(this)
                .load(area.getEastFile(this))
                .placeholder(R.drawable.ic_baseline_add_a_photo_24).fitCenter()
                .into(east);
        ImageButton south = findViewById(R.id.imageButton_south);
        Glide.with(this)
                .load(area.getSouthFile(this))
                .placeholder(R.drawable.ic_baseline_add_a_photo_24).fitCenter()
                .into(south);
        ImageButton west = findViewById(R.id.imageButton_west);
        Glide.with(this)
                .load(area.getWestFile(this))
                .placeholder(R.drawable.ic_baseline_add_a_photo_24).fitCenter()
                .into(west);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_area, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newArea) {
            area.delete(this);
        }
    }

    public void save(MenuItem item) {
        EditText areaName = findViewById(R.id.area_field_name);
        area.setName(areaName.getText().toString());
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