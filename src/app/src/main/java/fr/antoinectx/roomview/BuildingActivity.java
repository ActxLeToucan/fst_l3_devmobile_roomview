package fr.antoinectx.roomview;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;
import java.util.stream.Collectors;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.Building;

public class BuildingActivity extends MyActivity implements AreaRecyclerViewAdapter.ItemClickListener {
    private Building building;
    private AreaRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private TextInputEditText search;
    private String searchContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);

        building = Building.fromJSONString(getIntent().getStringExtra("building"));
        if (building == null) {
            finish();
            return;
        }

        initAppBar(building.getName(), getString(R.string.pagename_building), true);

        adapter = new AreaRecyclerViewAdapter(this, building.getAreas());
        adapter.setClickListener(this);
        recyclerView = findViewById(R.id.buildingActivity_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        search = findViewById(R.id.buildingActivity_searchBar);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchContent = s.toString();
                updateSearchResults();
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // TODO
    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_building, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    public void update() {
        updateData();
        updateInterface();
        updateSearchResults();
    }

    public void updateData() {
        boolean found = false;
        for (Building building : Building.loadAll(this)) {
            if (building.getId().compareTo(this.building.getId()) == 0) {
                this.building = building;
                found = true;
                break;
            }
        }
        // si l'objet n'existe plus, on ferme l'activité
        if (!found) {
            finish();
        }
    }

    public void updateInterface() {
        toolbar.setTitle(building.getName());
    }

    public void updateSearchResults() {
        adapter.setAreas(building.getAreas()
                .stream()
                .filter(area -> area.getName().toLowerCase().contains(searchContent.toLowerCase()))
                .collect(Collectors.toList()));
        if (!searchContent.isEmpty()) {
            toolbar.setSubtitle(adapter.getItemCount() + " " + (adapter.getItemCount() < 2 ?getString(R.string.searchMatch) : getString(R.string.searchMatches)));
        } else {
            toolbar.setSubtitle(getString(R.string.pagename_building));
        }
    }

    public void createArea(MenuItem item) {
        search.setText("");
        building.getAreas().add(new Area(getString(R.string.new_area), new Date()));
        building.save(this);
        update();
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        // TODO show a field to set the name and start the capture
    }

    public void showBuildingDetails(MenuItem item) {
        Intent intent = new Intent(this, EditBuildingActivity.class);
        intent.putExtra("building", building.toJSON().toString());
        startActivity(intent);
    }

    public void deleteBuilding(MenuItem item) {
        building.delete(this);
        finish();
    }
}