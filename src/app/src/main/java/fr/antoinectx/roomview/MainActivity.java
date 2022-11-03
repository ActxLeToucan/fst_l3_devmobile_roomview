package fr.antoinectx.roomview;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import fr.antoinectx.roomview.models.Batiment;
import fr.antoinectx.roomview.models.Donnees;

public class MainActivity extends AppCompatActivity implements BatimentRecyclerViewAdapter.ItemClickListener {
    private BatimentRecyclerViewAdapter adapter;
    private final Donnees donnees = new Donnees();
    private RecyclerView recyclerView;

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
        adapter = new BatimentRecyclerViewAdapter(this, donnees.getBatiments());
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onItemClick(View view, int position) {
        Batiment batiment = donnees.getBatiments().get(position);
        Toast.makeText(this, batiment.getNom(), Toast.LENGTH_SHORT).show();
    }

    public void createBuilding(MenuItem item) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        LocalDateTime now = LocalDateTime.now();

        donnees.getBatiments().add(new Batiment(getString(R.string.new_building), dateFormatter.format(now)));
        adapter.setBatiments(donnees.getBatiments());
        recyclerView.smoothScrollToPosition(donnees.getBatiments().size() - 1);
    }
}