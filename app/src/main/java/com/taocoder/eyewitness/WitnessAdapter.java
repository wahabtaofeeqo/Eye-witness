package com.taocoder.eyewitness;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class WitnessAdapter extends RecyclerView.Adapter<WitnessAdapter.ViewHolder> {

    private Context context;
    private List<Witness> witnesses;
    private OnFragmentChangeListener listener;

    public WitnessAdapter(Context context, List<Witness> witnesses, OnFragmentChangeListener listener) {
        this.context = context;
        this.witnesses = witnesses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.witness_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Witness witness = witnesses.get(position);

        if (position % 2 == 0)
            holder.v.setBackgroundColor(Color.rgb(255, 255, 255));
        else
            holder.v.setBackgroundColor(Color.TRANSPARENT);

        String[] a = witness.getDate().split(" ");
        holder.date.setText(a[0]);
        holder.type.setText(witness.getType().toUpperCase());
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onFragmentChange(DetailsFragment.getInstance(witness));
            }
        });
    }

    @Override
    public int getItemCount() {
        return witnesses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView date;
        private TextView type;
        private MaterialButton button;
        private View v;

        public ViewHolder(View view) {
            super(view);

            date = (TextView) view.findViewById(R.id.date);
            type = (TextView) view.findViewById(R.id.type);
            button = (MaterialButton) view.findViewById(R.id.view);
            v = view;
        }
    }
}