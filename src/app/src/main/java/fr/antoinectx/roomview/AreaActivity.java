package fr.antoinectx.roomview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.DirectionPhoto;
import fr.antoinectx.roomview.models.Passage;

public class AreaActivity extends PassageViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area);

        initAppBar(area.getName(), direction.getName(this), true);

        imageView = findViewById(R.id.passagesView_imageView);
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
        // hide "passages" if there is no image
        MenuItem passagesItem = menu.findItem(R.id.menu_area_passages);
        File photo = area.getFile(this, direction);
        passagesItem.setVisible(photo != null && photo.exists());

        // hide all options except "stop guided tour" if the guided tour is active
        if (pathPassages != null) {
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setVisible(false);
            }
        }
        menu.findItem(R.id.menu_area_stop_guided_tour).setVisible(pathPassages != null);

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
                    building.getAreas().forEach(a -> Arrays.asList(a.getDirectionPhotos()).forEach(p -> {
                        if (p != null && p.getPassages() != null) {
                            p.getPassages().removeIf(passage -> passage.getOtherSideId().equals(area.getId()));
                        }
                    }));
                    building.save(this);
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    public void editPassages(MenuItem item) {
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
        // update guided tour
        if (pathPassages != null && pathPassages.get(pathPassages.size() - 1).getOtherSideId().equals(area.getId())) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_baseline_map_24)
                    .setTitle(R.string.guidedTour)
                    .setMessage(R.string.tour_end_message)
                    .setPositiveButton("OK", null)
                    .show();
            pathPassages = null;
        }

        // update menu
        invalidateOptionsMenu();

        // update toolbar
        toolbar.setSubtitle(pathPassages == null ? direction.getName(this) : direction.getName(this) + " - " + getString(R.string.guidedTour));

        // update image
        applyImage(area.getFile(this, direction), new OnTouchListner() {
            @Override
            public void onSelection(SurfaceHolder surfaceHolder, Context parent, double[] imageSelection, double[] screenSelection) {
            }

            @Override
            public void afterSelection(SurfaceHolder surfaceHolder, Context parent, double[] imageSelection, SimpleClickEnabler clickEnabler) {
                clickEnabler.enable();
            }

            @Override
            public void onPassageClick(SurfaceHolder surfaceHolder, Context parent, Passage passage) {
                area = passage.getOtherSide(building.getAreas());
                update();
            }
        });

        // preload next images
        Glide.with(this)
                .load(area.getFile(this, direction.getLeft()))
                .preload();
        Glide.with(this)
                .load(area.getFile(this, direction.getRight()))
                .preload();

        DirectionPhoto directionPhoto = area.getDirectionPhoto(direction);

        // update date
        TextView dateTextView = findViewById(R.id.dateCapture);
        if (directionPhoto != null) {
            Date date = directionPhoto.getDate();
            String dateText = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
            dateTextView.setText(dateText);
        } else {
            dateTextView.setText("");
        }

        // update weather
        TextView weatherTextView = findViewById(R.id.weatherText);
        ImageView weatherIconView = findViewById(R.id.weatherIcon);
        if (directionPhoto != null) {
            String weather = directionPhoto.getWeather();
            String temperature = directionPhoto.getTemperature();
            String weatherIcon = directionPhoto.getIcon();
            if (weather != null && temperature != null && weatherIcon != null) {
                String weatherText = weather + ", " + temperature;
                weatherTextView.setText(weatherText);
                String iconUrl = "https://openweathermap.org/img/wn/" + weatherIcon + "@2x.png";
                weatherIconView.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(iconUrl)
                        .into(weatherIconView);
            } else {
                weatherTextView.setText("");
                weatherIconView.setImageDrawable(null);
                weatherIconView.setVisibility(View.GONE);
            }
        } else {
            weatherTextView.setText("");
            weatherIconView.setImageDrawable(null);
            weatherIconView.setVisibility(View.GONE);
        }

    }

    public void chooseDestination(MenuItem item) {
        selectArea(getString(R.string.destination), new OnSelectedAreaListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }

            @Override
            public void onSelect(Area area) {
                AlertDialog.Builder notReachableDialog = new AlertDialog.Builder(AreaActivity.this)
                        .setMessage(R.string.warning_areaNotReachable)
                        .setPositiveButton("OK", null);

                Area current = building.getAreas().stream().filter(a -> a.getId().equals(AreaActivity.this.area.getId())).findFirst().orElse(null);
                Area destination = building.getAreas().stream().filter(a -> a.getId().equals(area.getId())).findFirst().orElse(null);
                if (current == null || destination == null) {
                    notReachableDialog.show();
                    return;
                }

                DirectedMultigraph<Area, Passage> graph = building.getGraph();
                DijkstraShortestPath<Area, Passage> dijkstraAlg = new DijkstraShortestPath<>(graph);
                GraphPath<Area, Passage> path = dijkstraAlg.getPath(current, destination);

                if (path == null) {
                    notReachableDialog.show();
                    return;
                }

                new AlertDialog.Builder(AreaActivity.this)
                        .setIcon(R.drawable.ic_baseline_map_24)
                        .setTitle(R.string.steps)
                        .setMessage(path.getEdgeList().stream().map(passage -> " ??? " + passage.getOtherSide(building.getAreas()).getName()).collect(Collectors.joining("\n")))
                        .setPositiveButton(R.string.action_goThere, (dialog, which) -> {
                            AreaActivity.this.pathPassages = path.getEdgeList();
                            update();
                            new AlertDialog.Builder(AreaActivity.this)
                                    .setIcon(R.drawable.ic_baseline_map_24)
                                    .setTitle(R.string.guidedTour)
                                    .setMessage(R.string.tour_start_message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        })
                        .setNegativeButton(R.string.action_cancel, null)
                        .show();
            }

            @Override
            public void beforeDismiss() {

            }
        });
    }

    public void endGuidedTour(MenuItem item) {
        pathPassages = null;
        update();
    }
}