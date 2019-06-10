package com.app.petify.models;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.petify.R;

import java.util.List;

import static com.app.petify.models.Viaje.CHOFER_RESERVADO;
import static com.app.petify.models.Viaje.EN_CURSO;
import static com.app.petify.models.Viaje.FINALIZADO;

public class ViajeAdapter extends RecyclerView.Adapter<ViajeAdapter.ViewHolder> {
    private List<Viaje> dataset;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView from;
        public TextView to;
        public TextView date;
        public TextView price;
        public TextView status;

        public ViewHolder(View itemView) {
            super(itemView);
            from = itemView.findViewById(R.id.rv_item_from);
            to = itemView.findViewById(R.id.rv_item_to);
            date = itemView.findViewById(R.id.rv_item_date);
            price = itemView.findViewById(R.id.rv_item_price);
            status = itemView.findViewById(R.id.rv_item_status);
        }
    }

    public ViajeAdapter(List<Viaje> viajes, Context context) {
        dataset = viajes;
        this.context = context;
    }

    @Override
    public ViajeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_viajes, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.from.setText(dataset.get(position).origin_address);
        holder.from.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot, 0, 0, 0);
        holder.to.setText(dataset.get(position).destination_address);
        holder.to.setCompoundDrawablesWithIntrinsicBounds(R.drawable.place, 0, 0, 0);
        holder.date.setText(dataset.get(position).fecha);
        holder.date.setCompoundDrawablesWithIntrinsicBounds(R.drawable.date, 0, 0, 0);
        holder.price.setText(dataset.get(position).precio.toString());
        holder.price.setCompoundDrawablesWithIntrinsicBounds(R.drawable.money, 0, 0, 0);
        if (dataset.get(position).estado == EN_CURSO) {
            holder.status.setText("En curso");
            holder.status.setBackgroundResource(R.color.com_facebook_button_background_color);
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else if (dataset.get(position).estado == FINALIZADO) {
            holder.status.setText("Finalizado");
            holder.status.setBackgroundResource(R.color.grey);
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.dark));
        } else if (dataset.get(position).estado == CHOFER_RESERVADO) {
            holder.status.setText("Reserva");
            holder.status.setBackgroundResource(R.color.colorAccent);
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
