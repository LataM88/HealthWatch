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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainBoard extends AppCompatActivity {

    EditText nameText, dosageText, daysText;
    Button timeButton, addButton;
    TextView textViewError;
    String selectedTime = "";
    String apiKey = "";

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


        apiKey = getIntent().getStringExtra("apiKey");
        Log.d("MainBoard", "Otrzymano API Key: " + apiKey);


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

    private void sendMedicationToServer(String name, String dosage, String time, String days) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8080/api/medication";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("dosage", dosage);
            jsonBody.put("time", time);
            jsonBody.put("days", days);

            Log.d("sendMedicationToServer", "Wysyłane dane: " + jsonBody.toString());
        } catch (JSONException e) {
            Log.e("JSONError", "Błąd tworzenia JSON", e);
            textViewError.setText("Błąd tworzenia danych JSON!");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Log.d("ServerResponse", "Odpowiedź serwera: " + response.toString());
                    textViewError.setText("");
                    textViewError.setVisibility(View.GONE);
                    Toast.makeText(MainBoard.this, "Serwer: " + response.toString(), Toast.LENGTH_SHORT).show();
                },
                error -> {
                    String errorMessage = "Błąd połączenia: ";
                    if (error.networkResponse != null) {
                        errorMessage += "Kod błędu: " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            Log.e("ErrorResponse", "Błąd serwera: " + responseBody);
                            errorMessage += " - " + responseBody;
                        } catch (Exception e) {
                            errorMessage += " - Nie udało się odczytać odpowiedzi błędu.";
                            Log.e("ErrorResponse", "Wyjątek przy odczycie odpowiedzi błędu", e);
                        }
                    } else {
                        errorMessage += "Brak połączenia z serwerem.";
                    }
                    textViewError.setText(errorMessage);
                    textViewError.setVisibility(View.VISIBLE);
                    Log.e("RequestError", "Błąd żądania", error);
                }
        ) {
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
