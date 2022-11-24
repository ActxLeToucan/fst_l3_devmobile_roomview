package fr.antoinectx.roomview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import fr.antoinectx.roomview.models.Building;

public class EditBuildingActivity extends MyActivity {
    private Building building;
    private boolean editMode = false;
    private ImageButton photo;
    private boolean photoChanged = false;
    private File initPhoto;
    private File lastPhotoSelected;
    final private ActivityResultLauncher<Intent> chooseFromGalleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                                String picturePath = cursor.getString(columnIndex);

                                applyPhoto(picturePath);
                                cursor.close();
                            }
                        }
                    }
                }
            });
    private String pathPhotoFromCamera;
    final private ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        applyPhoto(pathPhotoFromCamera);
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

        initAppBar(building.getName(), getString(R.string.building_editing), true);

        photo = findViewById(R.id.editBuildingActivity_photo);
        photo.setOnClickListener(v -> selectImage(this));
        applyMode(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_building, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem edit = menu.findItem(R.id.menu_edit_building_editSave);
        edit.setIcon(editMode ? R.drawable.ic_baseline_save_24 : R.drawable.ic_baseline_edit_24);
        edit.setTitle(editMode ? R.string.action_save : R.string.action_edit);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (editMode && item.getItemId() == android.R.id.home) {
            applyMode(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lastPhotoSelected != null) {
            lastPhotoSelected.delete();
        }
    }

    public void editOrSaveBuilding(MenuItem item) {
        if (!editMode) {
            applyMode(true);
            return;
        }

        // set name
        TextInputEditText name = findViewById(R.id.building_field_name);
        building.setName(name.getText() != null ? name.getText().toString().trim() : "");

        // set description
        TextInputEditText description = findViewById(R.id.building_field_description);
        building.setDescription(description.getText() != null ? description.getText().toString() : "");

        // photo
        if (photoChanged) {
            photoChanged = false;
            File photoFile = building.getPhotoFile(this);
            if (photoFile != null) {
                if (initPhoto != null && !initPhoto.getPath().equals(photoFile.getPath())) {
                    initPhoto.delete();
                }
                // rename the photo to make it unique and avoid conflicts during the selection
                int i = photoFile.getPath().lastIndexOf('.');
                String extension = i > 0 ? photoFile.getPath().substring(i) : "";
                String timestamp = String.valueOf(System.currentTimeMillis()); // to invalidate cache
                File newFileName = new File(building.getDirectory(this), building.getId() + "_" + timestamp + extension);
                photoFile.renameTo(newFileName);
                building.setPhotoPath(newFileName.getName());
                // reset the temporary photo file to avoid deletion
                lastPhotoSelected = null;
                initPhoto = newFileName;
            }
        }


        building.save(this);
        applyMode(false);
    }

    private void applyMode(boolean editMode) {
        this.editMode = editMode;

        // fields
        View fields = findViewById(R.id.building_fields);
        fields.setVisibility(editMode ? View.VISIBLE : View.GONE);
        TextInputEditText name = findViewById(R.id.building_field_name);
        name.setEnabled(editMode);
        name.setText(building.getName());
        TextInputEditText description = findViewById(R.id.building_field_description);
        description.setEnabled(editMode);
        description.setText(building.getDescription());

        // text
        View text = findViewById(R.id.building_text);
        text.setVisibility(editMode ? View.GONE : View.VISIBLE);
        TextView nameText = findViewById(R.id.textView_building_name_content);
        nameText.setText(building.getName());
        TextView descriptionText = findViewById(R.id.textView_building_description_content);
        descriptionText.setText(building.getDescription());

        // photo
        photo.setEnabled(editMode);
        Glide.with(this)
                .load(initPhoto)
                .placeholder(editMode ? R.drawable.ic_baseline_add_a_photo_24 : R.drawable.ic_baseline_image_24)
                .into(photo);

        // appbar
        invalidateOptionsMenu();
        toolbar.setTitle(building.getName());
        toolbar.setSubtitle(getString(editMode ? R.string.building_editing : R.string.building_info));
        setAppBarNavigation(editMode ? R.drawable.ic_baseline_close_24 : R.drawable.ic_baseline_arrow_back_24,
                editMode ? R.string.action_cancel : R.string.action_back);
    }

    private void selectImage(Context context) {
        final CharSequence[] options = {getString(R.string.takePhoto), getString(R.string.fromGallery)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.add_photo));
        builder.setIcon(R.drawable.ic_baseline_add_a_photo_24);
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals(getString(R.string.takePhoto))) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 50);
                } else {
                    File f = createImageFile();
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri photoUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", f);
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    pathPhotoFromCamera = f.getAbsolutePath();

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

    private void applyPhoto(String picturePath) {
        File file = new File(picturePath);
        if (!file.exists()) {
            Log.e("EditBuildingActivity", "File does not exist");
            return;
        }

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
        building.setPhotoPath(newFile.getName());
        lastPhotoSelected = newFile;
        photoChanged = true;
        Glide.with(this)
                .load(building.getPhotoFile(this))
                .placeholder(R.drawable.ic_baseline_add_a_photo_24)
                .into(photo);
    }

    private File createImageFile() {
        String imageFileName = "temp_" + System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (Exception e) {
        }
        return image;
    }
}