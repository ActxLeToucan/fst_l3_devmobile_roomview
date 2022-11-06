package fr.antoinectx.roomview;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;

import fr.antoinectx.roomview.models.Building;

public class EditBuildingActivity extends MyActivity {
    private Building building;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_building);

        building = (Building) getIntent().getSerializableExtra("building");
        if (building == null) {
            finish();
            return;
        }

        initAppBar(building.getName(), getString(R.string.pagename_editbuilding), true);

        TextInputEditText name = findViewById(R.id.editBuildingActivity_name);
        name.setText(building.getName());
        TextInputEditText description = findViewById(R.id.editBuildingActivity_description);
        description.setText(building.getDescription());
        ImageButton photo = findViewById(R.id.editBuildingActivity_photo);
        if (building.getPhoto() != null) {
            photo.setImageBitmap(building.getPhoto());
        } else {
            photo.setImageResource(R.drawable.ic_baseline_add_a_photo_24);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_building, menu);
        return true;
    }

    public void saveBuilding(MenuItem item) {
        TextInputEditText name = findViewById(R.id.editBuildingActivity_name);
        TextInputEditText description = findViewById(R.id.editBuildingActivity_description);
        building.setName(name.getText() != null ? name.getText().toString() : "");
        building.setDescription(description.getText() != null ? description.getText().toString() : "");
        building.save(this);
        finish();
    }
}