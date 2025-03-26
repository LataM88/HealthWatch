package com.example.healtwatchp;

import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    TextView textViewRegisterNow;
    TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    Button buttonSubmit;
    String name, surname, email, password, apiKey;
    TextView textViewError;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        textViewRegisterNow = findViewById(R.id.txt_rejestruj);
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.haslo);
        buttonSubmit = findViewById(R.id.btn_login);
        textViewError = findViewById(R.id.blad);
        sharedPreferences = getSharedPreferences("MyAppName", MODE_PRIVATE);



        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = textInputEditTextEmail.getText() != null ? textInputEditTextEmail.getText().toString().trim() : "";
                password = textInputEditTextPassword.getText() != null ? textInputEditTextPassword.getText().toString().trim() : "";

                if (email.isEmpty() || password.isEmpty()) {
                    textViewError.setText("Wszystkie pola muszą być wypełnione!");
                    textViewError.setVisibility(View.VISIBLE);
                    return;
                } else {
                    textViewError.setVisibility(View.GONE);
                }

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://10.0.2.2:8080/api/login";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String status = jsonObject.getString("status");
                                    String message = jsonObject.getString("message");
                                    if (status.equals("succes")) {
                                        name = jsonObject.getString("name");
                                        surname = jsonObject.getString("surname");
                                        email = jsonObject.getString("email");
                                        apiKey = jsonObject.getString("apiKey");

                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("logged", "true");
                                        editor.putString("name", name);
                                        editor.putString("email", email);
                                        editor.putString("apiKey", apiKey);
                                        editor.apply();

                                        Intent intent = new Intent(getApplicationContext(), MainBoard.class);
                                        intent.putExtra("apiKey", apiKey);
                                        startActivity(intent);
                                        finish();
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
                        Map<String, String> paramV = new HashMap<>();
                        paramV.put("email", email);
                        paramV.put("password", password);
                        return paramV;
                    }
                };

                queue.add(stringRequest);
            }
        });
        textViewRegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Registration.class);
                startActivity(intent);
                finish();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}