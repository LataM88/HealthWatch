package com.example.healtwatchp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healtwatchp.adapter.Appointment;
import com.example.healtwatchp.adapter.AppointmentAdapter;
import com.example.healtwatchp.notifications.NotificationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AppointmentsBoard extends AppCompatActivity implements AppointmentAdapter.OnDeleteClickListener {

    EditText doctorNameText, dateText, notesText;
    Button timeButton, addButton, cancelButton;
    TextView textViewError;
    String selectedTime = "";
    String apiKey = "";
    FloatingActionButton fabAdd;
    CardView bottomPanel;

    RecyclerView recyclerView;
    AppointmentAdapter adapter;
    ArrayList<Appointment> appointmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointmentboard);

        doctorNameText = findViewById(R.id.doctor_name);
        dateText = findViewById(R.id.appointment_date);
        notesText = findViewById(R.id.appointment_notes);
        timeButton = findViewById(R.id.select_time);
        addButton = findViewById(R.id.add);
        cancelButton = findViewById(R.id.cancel_add);
        textViewError = findViewById(R.id.error_message);
        fabAdd = findViewById(R.id.fab_add);
        bottomPanel = findViewById(R.id.bottom_panel);
        recyclerView = findViewById(R.id.recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(appointmentList, this);
        recyclerView.setAdapter(adapter);

        apiKey = getIntent().getStringExtra("apiKey");
        bottomPanel.setVisibility(View.GONE);

        fetchAppointments();

        fabAdd.setOnClickListener(v -> {
            clearForm();
            bottomPanel.setVisibility(View.VISIBLE);
            fabAdd.hide();
        });

        cancelButton.setOnClickListener(v -> {
            bottomPanel.setVisibility(View.GONE);
            fabAdd.show();
        });

        timeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(AppointmentsBoard.this,
                    (view, hourOfDay, minute1) -> {
                        selectedTime = hourOfDay + ":" + (minute1 < 10 ? "0" + minute1 : minute1);
                        timeButton.setText(selectedTime);
                    },
                    hour, minute, true);
            timePickerDialog.show();
        });

        addButton.setOnClickListener(v -> {
            String doctorName = doctorNameText.getText().toString();
            String date = dateText.getText().toString();
            String notes = notesText.getText().toString();

            if (TextUtils.isEmpty(doctorName) || TextUtils.isEmpty(date) || TextUtils.isEmpty(selectedTime)) {
                textViewError.setText("Wszystkie pola muszą być wypełnione!");
                textViewError.setVisibility(View.VISIBLE);
                return;
            } else {
                textViewError.setVisibility(View.GONE);
                sendAppointmentToServer(doctorName, date, selectedTime, notes);
                // Integracja z NotificationHelper
                Appointment appointment = new Appointment(-1L, doctorName, date, selectedTime, notes);
                NotificationHelper notificationHelper = new NotificationHelper(AppointmentsBoard.this);
                notificationHelper.scheduleAppointmentNotification(appointment);
            }
        });
    }

    private void clearForm() {
        doctorNameText.setText("");
        dateText.setText("");
        notesText.setText("");
        timeButton.setText("Wybierz godzinę");
        selectedTime = "";
        textViewError.setVisibility(View.GONE);
    }

    private void fetchAppointments() {
        String url = "http://10.0.2.2:8080/api/appointments";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        appointmentList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            long id = jsonObject.optLong("id", -1);
                            if (id != -1) {
                                Appointment appointment = new Appointment(
                                        id,
                                        jsonObject.getString("doctorName"),
                                        jsonObject.getString("date"),
                                        jsonObject.getString("time"),
                                        jsonObject.optString("notes", "")
                                );
                                appointmentList.add(appointment);
                            }
                        }
                        adapter.updateData(new ArrayList<>(appointmentList));
                    } catch (JSONException e) {
                        Log.e("FetchAppointments", "Błąd parsowania JSON", e);
                    }
                },
                error -> {
                    Log.e("FetchAppointments", "Błąd żądania", error);
                    textViewError.setText("Błąd pobierania wizyt.");
                    textViewError.setVisibility(View.VISIBLE);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", apiKey);
                return headers;
            }
        };
        queue.add(jsonArrayRequest);
    }

    private void sendAppointmentToServer(String doctorName, String date, String time, String notes) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8080/api/appointment";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("doctorName", doctorName);
            jsonBody.put("date", date);
            jsonBody.put("time", time);
            jsonBody.put("notes", notes);
        } catch (JSONException e) {
            textViewError.setText("Błąd tworzenia danych JSON!");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    fetchAppointments();
                    Toast.makeText(AppointmentsBoard.this, "Wizyta została dodana!", Toast.LENGTH_SHORT).show();
                    bottomPanel.setVisibility(View.GONE);
                    fabAdd.show();
                },
                error -> {
                    textViewError.setText("Błąd dodawania wizyty.");
                    textViewError.setVisibility(View.VISIBLE);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", apiKey);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    @Override
    public void onDeleteClick(int position, Appointment appointment) {
        deleteAppointmentFromServer(appointment, position);
    }

    private void deleteAppointmentFromServer(Appointment appointment, int position) {
        if (appointment.getId() == null) {
            Toast.makeText(AppointmentsBoard.this, "Nie można usunąć wizyty bez ID!", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8080/api/appointment/" + appointment.getId();

        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    adapter.removeItem(position);
                    Toast.makeText(AppointmentsBoard.this, "Wizyta została usunięta!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(AppointmentsBoard.this, "Błąd usuwania wizyty!", Toast.LENGTH_SHORT).show();
                    Log.e("DeleteAppointment", "Błąd: " + error.toString());
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", apiKey);
                return headers;
            }
        };
        queue.add(deleteRequest);
    }
}