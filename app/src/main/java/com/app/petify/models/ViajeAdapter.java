package com.app.petify.models;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.petify.R;

public class ViajeAdapter extends RecyclerView.Adapter<ViajeAdapter.ViewHolder> {
    private Viaje[] dataset;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.item);
        }
    }

    public ViajeAdapter(Viaje[] viajes, Context context) {
        dataset = viajes;
        this.context = context;
    }

    @Override
    public ViajeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (dataset[position].getReserva())
            holder.tv.setTextColor(context.getColor(R.color.dark));
        else holder.tv.setTextColor(context.getColor(R.color.lightGrey));
        holder.tv.setText(dataset[position].getText());
    }

    @Override
    public int getItemCount() {
        return dataset.length;
    }
}
