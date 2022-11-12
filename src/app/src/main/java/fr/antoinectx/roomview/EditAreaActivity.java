package fr.antoinectx.roomview;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import fr.antoinectx.roomview.models.Area;

public class EditAreaActivity extends MyActivity {
    private Area area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_area);

        area = Area.fromJSONString(getIntent().getStringExtra("area"));
        if (area == null) {
            finish();
            return;
        }

        initAppBar(area.getName(), getString(R.string.pagename_editArea), true);

        EditText areaName = findViewById(R.id.area_field_name);
        areaName.setText(area.getName());

        ImageButton north = findViewById(R.id.imageButton_north);
        ImageButton east = findViewById(R.id.imageButton_east);
        ImageButton south = findViewById(R.id.imageButton_south);
        ImageButton west = findViewById(R.id.imageButton_west);
    }
}