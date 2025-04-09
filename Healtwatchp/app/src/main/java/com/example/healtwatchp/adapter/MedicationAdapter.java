package com.example.healtwatchp.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healtwatchp.R;

import java.util.ArrayList;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private ArrayList<Medication> medications;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position, Medication medication);
    }

    public MedicationAdapter(ArrayList<Medication> medications, OnDeleteClickListener listener) {
        this.medications = medications;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medications.get(position);
        holder.nameTextView.setText(medication.getName());
        holder.dosageTextView.setText("Dawka: " + medication.getDosage());
        holder.timeTextView.setText("Godzina: " + medication.getTime());
        holder.daysTextView.setText("Dni: " + medication.getDays());

        holder.deleteButton.setOnClickListener(view -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(holder.getAdapterPosition(), medication);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<Medication> newMedications) {
        this.medications.clear();
        this.medications.addAll(newMedications);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < medications.size()) {
            medications.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, dosageTextView, timeTextView, daysTextView;
        ImageButton deleteButton;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.medication_name);
            dosageTextView = itemView.findViewById(R.id.medication_dosage);
            timeTextView = itemView.findViewById(R.id.medication_time);
            daysTextView = itemView.findViewById(R.id.medication_days);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}