package fr.antoinectx.roomview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import fr.antoinectx.roomview.models.Batiment;

import java.util.List;

public class BatimentRecyclerViewAdapter extends RecyclerView.Adapter<BatimentRecyclerViewAdapter.ViewHolder> {
    private List<Batiment> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    BatimentRecyclerViewAdapter(Context context, List<Batiment> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.batiment_tile, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Batiment batiment = mData.get(position);
        holder.nomBatiment.setText(batiment.getNom());
        holder.descriptionBatiment.setText(batiment.getDescription());
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

        ViewHolder(View itemView) {
            super(itemView);
            nomBatiment = itemView.findViewById(R.id.textViewBatimentTileNom);
            descriptionBatiment = itemView.findViewById(R.id.textViewBatimentTileDescription);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Batiment getItem(int id) {
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

    public void setBatiments(List<Batiment> batiments) {
        mData = batiments;
        notifyDataSetChanged();
    }
}