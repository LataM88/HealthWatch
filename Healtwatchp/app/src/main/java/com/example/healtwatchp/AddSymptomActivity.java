package com.example.healtwatchp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddSymptomActivity extends AppCompatActivity {

    private TextInputEditText editTextSymptomName, editTextNotes;
    private TextView textViewDate, textViewTime, textViewIntensity, textViewError;
    private SeekBar seekBarIntensity;
    private Button buttonSelectDate, buttonSelectTime, buttonAddSymptom;

    private Calendar selectedDateTime;
    private String apiKey;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_symptom);

        initViews();
        setupListeners();

        sharedPreferences = getSharedPreferences("MyAppName", MODE_PRIVATE);
        apiKey = sharedPreferences.getString("apiKey", "");

        selectedDateTime = Calendar.getInstance();
        updateDateTimeDisplay();
    }

    private void initViews() {
        editTextSymptomName = findViewById(R.id.editTextSymptomName);
        editTextNotes = findViewById(R.id.editTextNotes);
        textViewDate = findViewById(R.id.textViewDate);
        textViewTime = findViewById(R.id.textViewTime);
        textViewIntensity = findViewById(R.id.textViewIntensity);
        textViewError = findViewById(R.id.textViewError);
        seekBarIntensity = findViewById(R.id.seekBarIntensity);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSelectTime = findViewById(R.id.buttonSelectTime);
        buttonAddSymptom = findViewById(R.id.buttonAddSymptom);

        // Ustawienie domyślnej wartości seekBar (5 z 10)
        seekBarIntensity.setProgress(4); // 0-9, więc 4 = 5
        textViewIntensity.setText("Nasilenie: 5/10");
    }

    private void setupListeners() {
        seekBarIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int intensity = progress + 1; // 1-10
                textViewIntensity.setText("Nasilenie: " + intensity + "/10");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonSelectTime.setOnClickListener(v -> showTimePicker());
        buttonAddSymptom.setOnClickListener(v -> addSymptom());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        textViewDate.setText("Data: " + dateFormat.format(selectedDateTime.getTime()));
        textViewTime.setText("Godzina: " + timeFormat.format(selectedDateTime.getTime()));
    }

    private void addSymptom() {
        String symptomName = editTextSymptomName.getText() != null ?
                editTextSymptomName.getText().toString().trim() : "";
        String notes = editTextNotes.getText() != null ?
                editTextNotes.getText().toString().trim() : "";

        if (symptomName.isEmpty()) {
            textViewError.setText("Nazwa objawu jest wymagana!");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        textViewError.setVisibility(View.GONE);

        // Format daty dla serwera: yyyy-MM-dd HH:mm:ss
        SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDateTime = serverDateFormat.format(selectedDateTime.getTime());

        int intensity = seekBarIntensity.getProgress() + 1; // 1-10

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8080/api/symptoms/add";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if (status.equals("success")) {
                                Toast.makeText(AddSymptomActivity.this, "Objaw został dodany!", Toast.LENGTH_SHORT).show();
                                finish(); // Wróć do poprzedniej aktywności
                            } else {
                                textViewError.setText(message);
                                textViewError.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            Log.e("JSONError", "Błąd parsowania JSON", e);
                            textViewError.setText("Błąd systemu. Spróbuj ponownie później.");
                            textViewError.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textViewError.setText("Błąd połączenia! Sprawdź internet.");
                textViewError.setVisibility(View.VISIBLE);
                Log.e("VolleyError", "Error: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("apiKey", apiKey);
                params.put("symptomName", symptomName);
                params.put("intensity", String.valueOf(intensity));
                params.put("symptomDate", formattedDateTime);
                if (!notes.isEmpty()) {
                    params.put("notes", notes);
                }
                return params;
            }
        };

        queue.add(stringRequest);
    }
}