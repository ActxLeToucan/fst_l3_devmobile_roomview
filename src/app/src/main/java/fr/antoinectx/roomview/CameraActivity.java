package fr.antoinectx.roomview;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.Surface;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import fr.antoinectx.roomview.models.Direction;

public class CameraActivity extends MyActivity {
    private float[] accelerometerValues = new float[3];
    private float[] magnetometerValues = new float[3];
    private SensorEventListener accelerometerListener, magnetometerListener;
    private Direction direction;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Executor executor;
    private File file;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        direction = Direction.valueOf(getIntent().getStringExtra("direction"));
        file = new File(getFilesDir(), "photo.jpg");

        setContentView(R.layout.activity_camera);

        initAppBar(direction.getName(this), getString(R.string.takePhoto), true, R.drawable.ic_baseline_close_24, R.string.action_cancel);
        previewView = findViewById(R.id.previewView);

        startSensors();
        startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(accelerometerListener);
        sensorManager.unregisterListener(magnetometerListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(accelerometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(magnetometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void startSensors() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        accelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                accelerometerValues = sensorEvent.values;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        magnetometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                magnetometerValues = sensorEvent.values;

                float[] result = new float[9];
                float[] orientation = new float[3];

                SensorManager.getRotationMatrix(result, null, accelerometerValues, magnetometerValues);
                SensorManager.getOrientation(result, orientation);

                float rad = orientation[0];

                // correction for other directions (not North)
                rad -= direction.ordinal() * (float) Math.PI / 2;

                // correction for landscape
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                rad += rotation * (float) Math.PI / 2;

                CompassView compassView = findViewById(R.id.compass);

                // normalize rad to -PI to PI
                rad = rad - 2 * (float) Math.PI * (float) Math.floor((rad + Math.PI) / (2 * Math.PI));
                compassView.setRadians(rad);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(magnetometerListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void startCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        } else {
            executor = ContextCompat.getMainExecutor(this);
            cameraProviderFuture = ProcessCameraProvider.getInstance(this);

            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }, executor);
        }
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // Set up the view finder use case to display camera preview
        Preview preview = new Preview.Builder().build();

        // Set up the capture use case to allow users to take photos
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(previewView.getDisplay() != null
                        ? previewView.getDisplay().getRotation() : Surface.ROTATION_0)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // Choose the camera by requiring a lens facing
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Attach use cases to the camera with the same lifecycle owner
        Camera camera = cameraProvider.bindToLifecycle(
                ((LifecycleOwner) this),
                cameraSelector,
                preview,
                imageCapture);

        // Connect the preview use case to the previewView
        preview.setSurfaceProvider(
                previewView.getSurfaceProvider());

        // set zoom to min
        camera.getCameraControl().setLinearZoom(0f);

        // pinch to zoom
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float currentZoomRatio = camera.getCameraInfo().getZoomState().getValue().getZoomRatio();
                float delta = detector.getScaleFactor();
                camera.getCameraControl().setZoomRatio(currentZoomRatio * delta);
                return true;
            }
        });
        previewView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });

        findViewById(R.id.captureButton).setOnClickListener(v -> {
                    ImageCapture.OutputFileOptions outputFileOptions =
                            new ImageCapture.OutputFileOptions.Builder(file).build();
                    imageCapture.takePicture(outputFileOptions, executor,
                            new ImageCapture.OnImageSavedCallback() {
                                @Override
                                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                    Log.d("CameraActivity", "Image saved");
                                    Intent intent = new Intent();
                                    intent.putExtra("direction", direction.toString());
                                    intent.putExtra("path", file.getAbsolutePath());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }

                                @Override
                                public void onError(@NonNull ImageCaptureException error) {
                                    Log.d("CameraActivity", "Image not saved");
                                    Intent intent = new Intent();
                                    intent.putExtra("direction", direction.toString());
                                    intent.putExtra("path", "");
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            }
                    );

                }
        );
    }


}