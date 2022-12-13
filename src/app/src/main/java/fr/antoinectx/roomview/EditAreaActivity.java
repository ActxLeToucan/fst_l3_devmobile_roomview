package fr.antoinectx.roomview;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.Building;
import fr.antoinectx.roomview.models.Direction;
import fr.antoinectx.roomview.models.DirectionPhoto;

public class EditAreaActivity extends MyActivity {
    private Building building;
    private Area oldArea;
    private Area area;
    private boolean saved = false;
    private boolean newArea;
    private ImageButton north;
    private ImageButton east;
    private ImageButton south;
    private ImageButton west;
    private Boolean addWeather = null;
    private String[] weather = {null, null, null};
    private Date lastWeatherUpdate = null;
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

                                if (addWeather == null) {
                                    new AlertDialog.Builder(this)
                                            .setTitle(R.string.weather)
                                            .setMessage(R.string.weather_add_message)
                                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                                addWeather = true;
                                                getWeather();
                                            })
                                            .setNegativeButton(R.string.no, (dialog, which) -> addWeather = false)
                                            .setOnCancelListener(null)
                                            .show();
                                } else if (addWeather) {
                                    getWeather();
                                }
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
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getWeather();
                }
            });

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

    private void beforeWeatherLoad() {
        runOnUiThread(() -> {
            FrameLayout loading = findViewById(R.id.loadingLayout_editArea);
            if (loading.getVisibility() == View.GONE) {
                AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
                inAnimation.setDuration(200);
                loading.setAnimation(inAnimation);
                loading.setVisibility(View.VISIBLE);
            }
        });
    }

    private void afterWeatherLoad() {
        runOnUiThread(() -> {
            FrameLayout loading = findViewById(R.id.loadingLayout_editArea);
            if (loading.getVisibility() == View.VISIBLE) {
                AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
                outAnimation.setDuration(200);
                loading.setAnimation(outAnimation);
                loading.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Get the weather from the API.
     * If the weather was loaded in the last 5 minutes, it will be loaded from {@link #weather}.
     */
    public void getWeather() {
        Date fiveMinutesAgo = new Date(System.currentTimeMillis() - 5 * 60 * 1000);
        if (lastWeatherUpdate != null && lastWeatherUpdate.after(fiveMinutesAgo)) {
            setWeather();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        beforeWeatherLoad();
        com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        afterWeatherLoad();
                        Toast.makeText(this, R.string.error_location, Toast.LENGTH_LONG).show();
                        return;
                    }

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=" + BuildConfig.WEATHER_API_KEY + "&units=metric&lang=fr";
                        try (InputStream is = new URL(url).openStream()) {
                            JSONObject json = new JSONObject(new Scanner(is).useDelimiter("\\A").next());
                            String weather = json.getJSONArray("weather").getJSONObject(0).getString("description");
                            String temperature = json.getJSONObject("main").getString("temp") + "°C";
                            String icon = json.getJSONArray("weather").getJSONObject(0).getString("icon");
                            runOnUiThread(() -> {
                                this.weather[0] = weather;
                                this.weather[1] = temperature;
                                this.weather[2] = icon;
                                lastWeatherUpdate = new Date();
                                setWeather();
                            });
                            afterWeatherLoad();
                        } catch (JSONException | IOException e) {
                            afterWeatherLoad();
                            e.printStackTrace();
                        }
                    });
                })
                .addOnFailureListener(this, e -> {
                    afterWeatherLoad();
                    e.printStackTrace();
                    Toast.makeText(this, R.string.error_location, Toast.LENGTH_LONG).show();
                })
                .addOnCanceledListener(this, () -> {
                    afterWeatherLoad();
                    Toast.makeText(this, R.string.error_location, Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Set the weather in each direction photo recently created (in the last 5 minutes).
     */
    public void setWeather() {
        Date fiveMinutesAgo = new Date(System.currentTimeMillis() - 5 * 60 * 1000);
        Arrays.stream(area.getDirectionPhotos()).forEach(directionPhoto -> {
            if (directionPhoto != null) {
                if (directionPhoto.getDate() != null && directionPhoto.getDate().after(fiveMinutesAgo)) {
                    directionPhoto.setWeather(weather[0]);
                    directionPhoto.setTemperature(weather[1]);
                    directionPhoto.setIcon(weather[2]);
                }
            }
        });
    }
}