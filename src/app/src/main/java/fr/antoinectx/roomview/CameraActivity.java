package fr.antoinectx.roomview;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ScaleGestureDetector;

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
    private Direction direction;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Executor executor;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        direction = Direction.valueOf(getIntent().getStringExtra("direction"));
        file = new File(getFilesDir(), "photo.jpg");

        setContentView(R.layout.activity_camera);

        initAppBar(direction.getName(this), getString(R.string.takePhoto), true, R.drawable.ic_baseline_close_24, R.string.action_cancel);
        previewView = findViewById(R.id.previewView);

        startCamera();
    }

    private void startCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 50);
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
                .setTargetRotation(previewView.getDisplay().getRotation())
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
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
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
                                public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                                    Log.d("CameraActivity", "Image saved");
                                    Intent intent = new Intent();
                                    intent.putExtra("direction", direction.toString());
                                    intent.putExtra("path", file.getAbsolutePath());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }

                                @Override
                                public void onError(ImageCaptureException error) {
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