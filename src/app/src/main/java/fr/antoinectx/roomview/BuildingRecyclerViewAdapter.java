package fr.antoinectx.roomview;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import fr.antoinectx.roomview.models.Building;

public class BuildingRecyclerViewAdapter extends RecyclerView.Adapter<BuildingRecyclerViewAdapter.ViewHolder> {
    private List<Building> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    BuildingRecyclerViewAdapter(Context context, List<Building> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.big_tile, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Building building = mData.get(position);
        holder.nomBatiment.setText(building.getName());
        holder.descriptionBatiment.setText(building.getDescription());
        Context context = holder.photoBatiment.getContext();
        File photo = building.getPhotoFile(context);
        if (photo != null && photo.exists()) {
            holder.photoBatiment.setImageBitmap(BitmapFactory.decodeFile(photo.getAbsolutePath()));
        } else {
            holder.photoBatiment.setImageResource(R.drawable.ic_baseline_image_24);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nomBatiment;
        TextView descriptionBatiment;
        ImageView photoBatiment;

        ViewHolder(View itemView) {
            super(itemView);
            nomBatiment = itemView.findViewById(R.id.bigTile_title);
            descriptionBatiment = itemView.findViewById(R.id.bigTile_description);
            photoBatiment = itemView.findViewById(R.id.bigTile_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Building getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setBuildings(List<Building> buildings) {
        mData = buildings;
        notifyDataSetChanged();
    }
}
