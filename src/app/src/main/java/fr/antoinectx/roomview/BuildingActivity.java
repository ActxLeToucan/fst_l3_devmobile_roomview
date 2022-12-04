package fr.antoinectx.roomview;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.stream.Collectors;

import fr.antoinectx.roomview.models.Building;
import fr.antoinectx.roomview.models.Direction;

public class BuildingActivity extends MyActivity implements AreaRecyclerViewAdapter.ItemClickListener {
    private Building building;
    final private ActivityResultLauncher<Intent> selectFileToExportTo = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();

                        new Thread(() -> {
                            AlertDialog.Builder dialogError = new AlertDialog.Builder(this)
                                    .setTitle(R.string.error)
                                    .setMessage(R.string.error_export)
                                    .setIcon(R.drawable.ic_baseline_error_24)
                                    .setPositiveButton("OK", null);
                            try {
                                if (building.export(this, uri)) {
                                    runOnUiThread(new AlertDialog.Builder(this)
                                            .setTitle(R.string.export_success_title)
                                            .setMessage(R.string.export_success_message)
                                            .setIcon(R.drawable.ic_baseline_check_circle_24)
                                            .setPositiveButton("OK", null)::show);
                                } else {
                                    runOnUiThread(dialogError::show);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(dialogError::show);
                            }
                        }).start();
                    }
                }
            });
    private AreaRecyclerViewAdapter adapter;
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
        RecyclerView recyclerView = findViewById(R.id.buildingActivity_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        TextInputEditText search = findViewById(R.id.buildingActivity_searchBar);
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

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(this, AreaActivity.class);
        intent.putExtra("building", building.toJSON().toString());
        intent.putExtra("area", adapter.getItem(position).toJSON().toString());
        intent.putExtra("direction", Direction.NORTH.toString());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Intent intent = new Intent(this, EditAreaActivity.class);
        intent.putExtra("building", building.toJSON().toString());
        intent.putExtra("area", adapter.getItem(position).toJSON().toString());
        startActivity(intent);
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
        if (!building.reload(this)) {
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
            toolbar.setSubtitle(adapter.getItemCount() + " " + (adapter.getItemCount() < 2 ? getString(R.string.searchMatch) : getString(R.string.searchMatches)));
        } else {
            toolbar.setSubtitle(getString(R.string.pagename_building));
        }
    }

    public void createArea(MenuItem item) {
        Intent intent = new Intent(this, EditAreaActivity.class);
        intent.putExtra("building", building.toJSON().toString());
        startActivity(intent);
    }

    public void showBuildingDetails(MenuItem item) {
        Intent intent = new Intent(this, EditBuildingActivity.class);
        intent.putExtra("building", building.toJSON().toString());
        startActivity(intent);
    }

    public void exportBuilding(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_TITLE, building.getName() + ".building");
        selectFileToExportTo.launch(intent);
    }

    public void deleteBuilding(MenuItem item) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setTitle(R.string.warning)
                .setMessage(R.string.warning_deleteBuilding)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    building.delete(this);
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}