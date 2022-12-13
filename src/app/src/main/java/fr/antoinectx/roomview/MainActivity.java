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
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.antoinectx.roomview.models.Building;

public class MainActivity extends MyActivity implements BuildingRecyclerViewAdapter.ItemClickListener {
    private final List<Building> buildings = new ArrayList<>();
    private BuildingRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private String searchContent = "";
    final private ActivityResultLauncher<Intent> selectFileToImport = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();

                        new Thread(() -> {
                            beforeImport();
                            AlertDialog.Builder dialogError = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.error)
                                    .setMessage(R.string.error_import)
                                    .setIcon(R.drawable.ic_baseline_error_24)
                                    .setPositiveButton("OK", null);
                            try {
                                if (!Building.importFrom(MainActivity.this, uri)) {
                                    MainActivity.this.runOnUiThread(dialogError::show);
                                    afterImport();
                                    return;
                                }

                                MainActivity.this.runOnUiThread(() -> {
                                    try {
                                        MainActivity.this.update();
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle(R.string.import_success_title)
                                                .setMessage(R.string.import_success_message)
                                                .setIcon(R.drawable.ic_baseline_check_circle_24)
                                                .setPositiveButton("OK", null)
                                                .show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        dialogError.show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                MainActivity.this.runOnUiThread(dialogError::show);
                            }
                            afterImport();
                        }).start();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAppBar(getString(R.string.app_name), getString(R.string.pagename_main), false);

        adapter = new BuildingRecyclerViewAdapter(this, buildings);
        adapter.setClickListener(this);
        recyclerView = findViewById(R.id.mainActivity_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        updateData();

        TextInputEditText search = findViewById(R.id.mainActivity_searchBar);
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
    protected void onPause() {
        super.onPause();
        saveBuildings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
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
        intent.putExtra("building", building.toJSON().toString());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Intent intent = new Intent(this, EditBuildingActivity.class);
        intent.putExtra("building", buildings.get(position).toJSON().toString());
        startActivity(intent);
    }

    public void saveBuildings() {
        for (Building building : buildings) {
            building.save(this);
        }
    }

    public void update() {
        updateData();
        updateSearchResults();
    }

    public void updateData() {
        buildings.clear();
        buildings.addAll(Building.loadAll(this));
    }

    public void updateSearchResults() {
        adapter.setBuildings(buildings
                .stream()
                .filter(building -> (building.getName().toLowerCase().contains(searchContent.toLowerCase()) ||
                        building.getDescription().toLowerCase().contains(searchContent.toLowerCase())))
                .collect(Collectors.toList()));
        if (!searchContent.isEmpty()) {
            toolbar.setSubtitle(adapter.getItemCount() + " " + (adapter.getItemCount() < 2 ? getString(R.string.searchMatch) : getString(R.string.searchMatches)));
        } else {
            toolbar.setSubtitle(getString(R.string.pagename_main));
        }
    }

    public void createBuilding(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.add_building));
        View layout = getLayoutInflater().inflate(R.layout.dialog_create_building, null);
        builder.setView(layout);

        builder.setPositiveButton("OK", (dialogInterface, which) -> {
            TextInputEditText fieldName = layout.findViewById(R.id.building_field_name);
            String name = fieldName.getText() == null || fieldName.getText().toString().trim().isEmpty() ? getString(R.string.building_new) : fieldName.getText().toString().trim();
            TextInputEditText fieldDescription = layout.findViewById(R.id.building_field_description);
            String description = fieldDescription.getText() == null ? "" : fieldDescription.getText().toString().trim();

            Building building = new Building(name, description);
            building.save(this);
            update();
            recyclerView.smoothScrollToPosition(buildings.size() - 1);
        });

        builder.setNegativeButton(getString(R.string.action_cancel), (dialogInterface, which) -> dialogInterface.cancel());

        builder.setNeutralButton(R.string.action_import, (dialogInterface, which) -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/zip");
            selectFileToImport.launch(intent);
        });

        builder.show();
    }

    private void beforeImport() {
        runOnUiThread(() -> {
            FrameLayout loading = findViewById(R.id.loadingLayout_import);
            if (loading.getVisibility() == View.GONE) {
                AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
                inAnimation.setDuration(200);
                loading.setAnimation(inAnimation);
                loading.setVisibility(View.VISIBLE);
            }
        });
    }

    private void afterImport() {
        runOnUiThread(() -> {
            FrameLayout loading = findViewById(R.id.loadingLayout_import);
            if (loading.getVisibility() == View.VISIBLE) {
                AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
                outAnimation.setDuration(200);
                loading.setAnimation(outAnimation);
                loading.setVisibility(View.GONE);
            }
        });
    }
}