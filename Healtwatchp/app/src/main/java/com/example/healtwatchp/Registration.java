package com.example.healtwatchp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    TextInputEditText textInputEditTextName, textInputEditTextSurname, textInputEditTextEmail, textInputEditTextPassword;
    Button buttonSubmit;

    String name, surname, email, password;
    TextView textViewError, textViewLoginNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        textInputEditTextName = findViewById(R.id.imie);
        textInputEditTextSurname = findViewById(R.id.nazwisko);
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.haslo);
        textViewError = findViewById(R.id.blad);
        buttonSubmit = findViewById(R.id.btn_rejestracja);
        textViewLoginNow = findViewById(R.id.txt_zaloguj);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = String.valueOf(textInputEditTextName.getText());
                surname = String.valueOf(textInputEditTextSurname.getText());
                email = String.valueOf(textInputEditTextEmail.getText());
                password = String.valueOf(textInputEditTextPassword.getText());

                if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    textViewError.setText("Wszystkie pola muszą być wypełnione!");
                    textViewError.setVisibility(View.VISIBLE);
                    return;
                } else {
                    textViewError.setVisibility(View.GONE);
                }

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://10.0.2.2:8080/api/register";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.equals("success")) {
                                    Toast.makeText(getApplicationContext(), "Rejestracja zakończona sukcesem", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    textViewError.setText("Rejestracja nie powiodła się. Spróbuj ponownie.");
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
                        Map<String, String> paramV = new HashMap<>();
                        paramV.put("name", name);
                        paramV.put("surname", surname);
                        paramV.put("email", email);
                        paramV.put("password", password);
                        return paramV;
                    }
                };

                queue.add(stringRequest);
            }
        });
        textViewLoginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}