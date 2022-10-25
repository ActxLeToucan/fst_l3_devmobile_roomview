package fr.antoinectx.roomview;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import fr.antoinectx.roomview.models.Batiment;
import fr.antoinectx.roomview.models.Donnees;

public class MainActivity extends AppCompatActivity implements BatimentRecyclerViewAdapter.ItemClickListener {
    private BatimentRecyclerViewAdapter adapter;
    private final Donnees donnees = new Donnees();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBatiments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BatimentRecyclerViewAdapter(this, donnees.getBatiments());
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        Batiment batiment = donnees.getBatiments().get(position);
        Toast.makeText(this, batiment.getNom(), Toast.LENGTH_SHORT).show();
    }
}