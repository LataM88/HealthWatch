package com.example.healtwatchp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healtwatchp.R;
import com.example.healtwatchp.models.Symptom;

import java.util.List;

public class SymptomAdapter extends RecyclerView.Adapter<SymptomAdapter.SymptomViewHolder> {

    private Context context;
    private List<Symptom> symptoms;
    private OnSymptomClickListener listener;

    public interface OnSymptomClickListener {
        void onDeleteClick(Symptom symptom, int position);
        void onItemClick(Symptom symptom);
    }

    public SymptomAdapter(Context context, List<Symptom> symptoms) {
        this.context = context;
        this.symptoms = symptoms;
    }

    public void setOnSymptomClickListener(OnSymptomClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_symptom, parent, false);
        return new SymptomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {
        Symptom symptom = symptoms.get(position);

        holder.textViewSymptomName.setText(symptom.getSymptomName());
        holder.textViewDate.setText(symptom.getFormattedDate());
        holder.textViewIntensity.setText(symptom.getIntensity() + "/10");
        holder.textViewIntensityDesc.setText(symptom.getIntensityDescription());

        // Ustawienie paska postępu
        holder.progressBarIntensity.setProgress(symptom.getIntensity() * 10); // 0-100%

        // Kolor paska w zależności od intensywności
        if (symptom.getIntensity() <= 3) {
            holder.progressBarIntensity.setProgressTintList(
                    context.getResources().getColorStateList(android.R.color.holo_green_light, null));
        } else if (symptom.getIntensity() <= 6) {
            holder.progressBarIntensity.setProgressTintList(
                    context.getResources().getColorStateList(android.R.color.holo_orange_light, null));
        } else {
            holder.progressBarIntensity.setProgressTintList(
                    context.getResources().getColorStateList(android.R.color.holo_red_light, null));
        }

        // Notatki - pokaż tylko jeśli istnieją
        if (symptom.getNotes() != null && !symptom.getNotes().trim().isEmpty()) {
            holder.textViewNotes.setVisibility(View.VISIBLE);
            holder.textViewNotes.setText(symptom.getNotes());
        } else {
            holder.textViewNotes.setVisibility(View.GONE);
        }

        // Kliknięcie na cały element
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(symptom);
            }
        });

        // Kliknięcie na przycisk usuwania
        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(symptom, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return symptoms.size();
    }

    public void removeItem(int position) {
        symptoms.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, symptoms.size());
    }

    public void updateSymptoms(List<Symptom> newSymptoms) {
        this.symptoms.clear();
        this.symptoms.addAll(newSymptoms);
        notifyDataSetChanged();
    }

    static class SymptomViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSymptomName, textViewDate, textViewIntensity,
                textViewIntensityDesc, textViewNotes;
        ProgressBar progressBarIntensity;
        ImageButton buttonDelete;

        public SymptomViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSymptomName = itemView.findViewById(R.id.textViewSymptomName);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewIntensity = itemView.findViewById(R.id.textViewIntensity);
            textViewIntensityDesc = itemView.findViewById(R.id.textViewIntensityDesc);
            textViewNotes = itemView.findViewById(R.id.textViewNotes);
            progressBarIntensity = itemView.findViewById(R.id.progressBarIntensity);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}