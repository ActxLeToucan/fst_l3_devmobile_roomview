package fr.antoinectx.roomview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import fr.antoinectx.roomview.models.Building;

public class EditBuildingActivity extends MyActivity {
    private Building building;
    private ImageButton photo;
    private File initPhoto;
    private File lastPhotoSelected;

    ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                if (result.getResultCode() == RESULT_OK) {
                    // TODO: handle photo
                }
            });
    ActivityResultLauncher<Intent> chooseFromGalleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                                String picturePath = cursor.getString(columnIndex);
                                File file = new File(picturePath);

                                File buildingDir = building.getDirectory(this);
                                File newFile = new File(buildingDir, file.getName());

                                // remove the last photo selected
                                if (lastPhotoSelected != null) lastPhotoSelected.delete();

                                // copy file to app's private storage
                                try {
                                    Files.copy(file.toPath(), newFile.toPath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // update building's photo
                                building.setPhoto(newFile.getName());
                                lastPhotoSelected = newFile;
                                photo.setImageBitmap(BitmapFactory.decodeFile(newFile.getAbsolutePath()));
                                cursor.close();
                            }
                        }
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_building);

        building = Building.fromJSONString(getIntent().getStringExtra("building"));
        if (building == null) {
            finish();
            return;
        }
        initPhoto = building.getPhotoFile(this);

        initAppBar(building.getName(), getString(R.string.pagename_editbuilding), true);

        TextInputEditText name = findViewById(R.id.editBuildingActivity_name);
        name.setText(building.getName());
        TextInputEditText description = findViewById(R.id.editBuildingActivity_description);
        description.setText(building.getDescription());
        photo = findViewById(R.id.editBuildingActivity_photo);
        if (building.getPhotoPath() != null && !building.getPhotoPath().isEmpty()) {
            photo.setImageBitmap(BitmapFactory.decodeFile(building.getPhotoFile(this).getAbsolutePath()));
        } else {
            photo.setImageResource(R.drawable.ic_baseline_add_a_photo_24);
        }
        photo.setOnClickListener(v -> {
            selectImage(this);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_building, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lastPhotoSelected != null) {
            lastPhotoSelected.delete();
        }
    }

    public void saveBuilding(MenuItem item) {
        // set name
        TextInputEditText name = findViewById(R.id.editBuildingActivity_name);
        building.setName(name.getText() != null ? name.getText().toString() : "");

        // set description
        TextInputEditText description = findViewById(R.id.editBuildingActivity_description);
        building.setDescription(description.getText() != null ? description.getText().toString() : "");

        // photo
        File photoFile = building.getPhotoFile(this);
        if (photoFile != null) {
            if (initPhoto != null && !initPhoto.getPath().equals(photoFile.getPath())) {
                initPhoto.delete();
            }
            // rename the photo to make it unique and avoid conflicts during the selection
            File newFileName = new File(building.getDirectory(this), building.getId() + '_' + photoFile.getName());
            photoFile.renameTo(newFileName);
            building.setPhoto(newFileName.getName());
            // reset the temporary photo file to avoid deletion
            lastPhotoSelected = null;
        }


        building.save(this);
        finish();
    }

    private void selectImage(Context context) {
        final CharSequence[] options = { getString(R.string.takePhoto), getString(R.string.fromGallery) };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.addPhoto));
        builder.setIcon(R.drawable.ic_baseline_add_a_photo_24);
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals(getString(R.string.takePhoto))) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 50);
                } else {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePhotoLauncher.launch(takePicture);
                }
            } else if (options[item].equals(getString(R.string.fromGallery))) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    chooseFromGalleryLauncher.launch(intent);
                }
            }
        });
        builder.show();
    }
}