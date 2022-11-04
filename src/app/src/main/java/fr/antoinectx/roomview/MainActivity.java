package fr.antoinectx.roomview;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import fr.antoinectx.roomview.models.Building;
import fr.antoinectx.roomview.models.Passage;
import fr.antoinectx.roomview.models.Photo;
import fr.antoinectx.roomview.models.Zone;

public class MainActivity extends AppCompatActivity implements BatimentRecyclerViewAdapter.ItemClickListener {
    private BatimentRecyclerViewAdapter adapter;
    private final List<Building> buildings = new ArrayList<>();
    private RecyclerView recyclerView;
    private EditText search;
    private int downX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.pagename_main);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewBatiments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BatimentRecyclerViewAdapter(this, buildings);
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);

        loadBuildings();

        search = findViewById(R.id.searchBarBatiments);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setBatiments(buildings
                        .stream()
                        .filter(building -> (building.getNom().toLowerCase().contains(s.toString().toLowerCase()) ||
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

    /**
     * Clear focus on the search bar when the user clicks outside of it
     * from https://stackoverflow.com/a/61290481
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = (int) event.getRawX();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                //Was it a scroll - If skip all
                if (Math.abs(downX - x) > 5) {
                    return super.dispatchTouchEvent(event);
                }
                final int reducePx = 25;
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                //Bounding box is to big, reduce it just a little bit
                outRect.inset(reducePx, reducePx);
                if (!outRect.contains(x, y)) {
                    v.clearFocus();
                    boolean touchTargetIsEditText = false;
                    //Check if another editText has been touched
                    for (View vi : v.getRootView().getTouchables()) {
                        if (vi instanceof EditText) {
                            Rect clickedViewRect = new Rect();
                            vi.getGlobalVisibleRect(clickedViewRect);
                            //Bounding box is to big, reduce it just a little bit
                            clickedViewRect.inset(reducePx, reducePx);
                            if (clickedViewRect.contains(x, y)) {
                                touchTargetIsEditText = true;
                                break;
                            }
                        }
                    }
                    if (!touchTargetIsEditText) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onItemClick(View view, int position) {
        Building building = buildings.get(position);
        Toast.makeText(this, building.getNom(), Toast.LENGTH_SHORT).show();
    }

    public void loadBuildings() {
        buildings.clear();
        buildings.addAll(Building.loadAll(this));
        adapter.setBatiments(buildings);
    }

    public void createBuilding(MenuItem item) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        LocalDateTime now = LocalDateTime.now();

        Building bat = new Building(getString(R.string.new_building), dateFormatter.format(now));
        Zone zone1 = new Zone("Zone 1", new Date());
        Zone zone2 = new Zone("Zone 2", new Date());

        Photo photo1Sud = new Photo();
        Photo photo2Nord = new Photo();

        photo1Sud.getPassages().add(new Passage(0, 0, 0, 0, zone2));
        photo2Nord.getPassages().add(new Passage(0, 0, 0, 0, zone1));

        zone1.setPhotoNord(new Photo());
        zone1.setPhotoEst(new Photo());
        zone1.setPhotoSud(photo1Sud);
        zone1.setPhotoOuest(new Photo());
        zone2.setPhotoNord(photo2Nord);
        zone2.setPhotoEst(new Photo());
        zone2.setPhotoSud(new Photo());
        zone2.setPhotoOuest(new Photo());

        bat.getZones().add(zone1);
        bat.getZones().add(zone2);
        bat.save(this);

        buildings.add(bat);
        adapter.setBatiments(buildings);
        recyclerView.smoothScrollToPosition(buildings.size() - 1);
    }
}