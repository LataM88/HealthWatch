package com.example.healtwatchp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healtwatchp.R;

import java.util.ArrayList;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private ArrayList<Appointment> appointments;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position, Appointment appointment);
    }

    public AppointmentAdapter(ArrayList<Appointment> appointments, OnDeleteClickListener listener) {
        this.appointments = appointments;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.doctorNameTextView.setText(appointment.getDoctorName());
        holder.dateTextView.setText("Data: " + appointment.getDate());
        holder.timeTextView.setText("Godzina: " + appointment.getTime());
        holder.notesTextView.setText("Notatki: " + appointment.getNotes());

        holder.deleteButton.setOnClickListener(view -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(holder.getAdapterPosition(), appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateData(ArrayList<Appointment> newAppointments) {
        this.appointments.clear();
        this.appointments.addAll(newAppointments);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < appointments.size()) {
            appointments.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView doctorNameTextView, dateTextView, timeTextView, notesTextView;
        ImageButton deleteButton;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorNameTextView = itemView.findViewById(R.id.appointment_doctor_name);
            dateTextView = itemView.findViewById(R.id.appointment_date);
            timeTextView = itemView.findViewById(R.id.appointment_time);
            notesTextView = itemView.findViewById(R.id.appointment_notes);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}