package fr.antoinectx.roomview;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.Building;
import fr.antoinectx.roomview.models.Passage;
import fr.antoinectx.roomview.models.Photo;

public class MainActivity extends MyActivity implements BuildingRecyclerViewAdapter.ItemClickListener {
    private final List<Building> buildings = new ArrayList<>();
    private BuildingRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyMaterialToolbar(getString(R.string.app_name), getString(R.string.pagename_main), false);

        adapter = new BuildingRecyclerViewAdapter(this, buildings);
        adapter.setClickListener(this);
        recyclerView = findViewById(R.id.mainActivity_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadBuildings();

        EditText search = findViewById(R.id.mainActivity_searchBar);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setBatiments(buildings
                        .stream()
                        .filter(building -> (building.getName().toLowerCase().contains(s.toString().toLowerCase()) ||
                                    building.getDescription().toLowerCase().contains(s.toString().toLowerCase())))
                        .collect(Collectors.toList()));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        search.setOnFocusChangeListener((view, b) -> {
            if (b) {
                toolbar.setSubtitle(R.string.searchBuilding);
            } else {
                toolbar.setSubtitle(R.string.pagename_main);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveBuildings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBuildings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onItemClick(View view, int position) {
        Building building = buildings.get(position);

        Intent intent = new Intent(this, BuildingActivity.class);
        intent.putExtra("building", building);
        startActivity(intent);
    }

    public void loadBuildings() {
        buildings.clear();
        buildings.addAll(Building.loadAll(this));
    }

    public void saveBuildings() {
        for (Building building : buildings) {
            building.save(this);
        }
    }

    public void createBuilding(MenuItem item) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        LocalDateTime now = LocalDateTime.now();

        Building bat = new Building(getString(R.string.new_building), dateFormatter.format(now));
        Area area1 = new Area("Zone 1", new Date());
        Area area2 = new Area("Zone 2", new Date());

        Photo photo1Sud = new Photo();
        Photo photo2Nord = new Photo();

        photo1Sud.getPassages().add(new Passage(0, 0, 0, 0, area2));
        photo2Nord.getPassages().add(new Passage(0, 0, 0, 0, area1));

        area1.setPhotoNord(new Photo());
        area1.setPhotoEst(new Photo());
        area1.setPhotoSud(photo1Sud);
        area1.setPhotoOuest(new Photo());
        area2.setPhotoNord(photo2Nord);
        area2.setPhotoEst(new Photo());
        area2.setPhotoSud(new Photo());
        area2.setPhotoOuest(new Photo());

        bat.getAreas().add(area1);
        bat.getAreas().add(area2);
        bat.save(this);

        buildings.add(bat);
        adapter.setBatiments(buildings);
        recyclerView.smoothScrollToPosition(buildings.size() - 1);
    }
}