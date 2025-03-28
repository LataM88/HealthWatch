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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.healtwatchp.adapter.Medication;
import com.example.healtwatchp.adapter.MedicationAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainBoard extends AppCompatActivity {

    EditText nameText, dosageText, daysText;
    Button timeButton, addButton;
    TextView textViewError;
    String selectedTime = "";
    String apiKey = "";

    RecyclerView recyclerView;
    MedicationAdapter adapter;
    ArrayList<Medication> medicationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainboard);

        nameText = findViewById(R.id.name);
        dosageText = findViewById(R.id.dosage);
        daysText = findViewById(R.id.selected_days);
        timeButton = findViewById(R.id.select_time);
        addButton = findViewById(R.id.add);
        textViewError = findViewById(R.id.error_message);

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        medicationList = new ArrayList<>();
        adapter = new MedicationAdapter(medicationList);
        recyclerView.setAdapter(adapter);

        apiKey = getIntent().getStringExtra("apiKey");
        Log.d("MainBoard", "Otrzymano API Key: " + apiKey);

        fetchMedications();

        timeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(MainBoard.this,
                    (view, hourOfDay, minute1) -> {
                        selectedTime = hourOfDay + ":" + (minute1 < 10 ? "0" + minute1 : minute1);
                        timeButton.setText(selectedTime);
                    },
                    hour, minute, true);
            timePickerDialog.show();
        });

        addButton.setOnClickListener(v -> {
            String name = nameText.getText().toString();
            String dosage = dosageText.getText().toString();
            String days = daysText.getText().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dosage) || TextUtils.isEmpty(selectedTime) || TextUtils.isEmpty(days)) {
                textViewError.setText("Wszystkie pola muszą być wypełnione!");
                textViewError.setVisibility(View.VISIBLE);
                return;
            } else {
                textViewError.setVisibility(View.GONE);
                sendMedicationToServer(name, dosage, selectedTime, days);
            }
        });
    }

    private void fetchMedications() {
        String url = "http://10.0.2.2:8080/api/medications";
        Log.d("FetchMedications", "Rozpoczynam pobieranie leków");

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        medicationList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            Log.d("FetchMedications", "Dodawanie leku: " + jsonObject);
                            Medication medication = new Medication(
                                    jsonObject.getString("name"),
                                    jsonObject.getString("dosage"),
                                    jsonObject.getString("time"),
                                    jsonObject.getString("days")
                            );
                            medicationList.add(medication);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("FetchMedications", "Błąd parsowania JSON", e);
                    }
                },
                error -> {
                    Log.e("FetchMedications", "Błąd żądania", error);
                    textViewError.setText("Błąd pobierania leków.");
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

    private void sendMedicationToServer(String name, String dosage, String time, String days) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8080/api/medication";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("dosage", dosage);
            jsonBody.put("time", time);
            jsonBody.put("days", days);
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
                    fetchMedications();
                    Toast.makeText(MainBoard.this, "Lek został dodany!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    textViewError.setText("Błąd dodawania leku.");
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
}
