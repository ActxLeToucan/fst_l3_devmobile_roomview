package fr.antoinectx.roomview;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.stream.Collectors;

import fr.antoinectx.roomview.models.Building;

public class BuildingActivity extends MyActivity implements AreaRecyclerViewAdapter.ItemClickListener {
    private Building building;
    private AreaRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);

        building = (Building) getIntent().getSerializableExtra("building");
        if (building == null) {
            finish();
            return;
        }

        applyMaterialToolbar(building.getName(), getString(R.string.pagename_building), true);

        adapter = new AreaRecyclerViewAdapter(this, building.getAreas());
        adapter.setClickListener(this);
        recyclerView = findViewById(R.id.buildingActivity_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        EditText search = findViewById(R.id.buildingActivity_searchBar);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setAreas(building.getAreas()
                        .stream()
                        .filter(area -> area.getName().toLowerCase().contains(s.toString().toLowerCase()))
                        .collect(Collectors.toList()));
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

    // TODO
    public void createArea(MenuItem item) {

    }

    // TODO
    public void editBuilding(MenuItem item) {

    }

    // TODO
    public void deleteBuilding(MenuItem item) {

    }
}