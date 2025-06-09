package com.example.healtwatchp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MedicineInfo extends AppCompatActivity {
    private static final String TAG = "MedicineInfo";

    // ZMIEŃ TEN URL NA ADRES TWOJEGO API
    // Jeśli testujesz lokalnie, użyj swojego IP (np. 192.168.1.100:5000)
    // Jeśli API jest na serwerze, wstaw właściwy URL
    private static final String API_BASE_URL = "http://10.0.2.2:5000"; // ZMIEŃ NA SWÓJ IP!

    private AutoCompleteTextView medicineNameInput;
    private TextView medicineInfoText;
    private Button searchButton;
    private ArrayAdapter<String> adapter;
    private List<String> suggestions = new ArrayList<>();
    private RequestQueue requestQueue;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_info);

        // Drawer and Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(true);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_medications) {
                    Intent intent = new Intent(MedicineInfo.this, MainBoard.class);
                    intent.putExtra("apiKey", getIntent().getStringExtra("apiKey")); // Pass the apiKey
                    startActivity(intent);
                } else if (id == R.id.nav_appointments) {
                    Intent intent = new Intent(MedicineInfo.this, AppointmentsBoard.class);
                    intent.putExtra("apiKey", getIntent().getStringExtra("apiKey")); // Pass the apiKey
                    startActivity(intent);
                } else if (id == R.id.nav_medicine_info) {
                    // Already here
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        medicineNameInput = findViewById(R.id.medicine_name_input);
        medicineInfoText = findViewById(R.id.medicine_info_text);
        searchButton = findViewById(R.id.search_button);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        medicineNameInput.setAdapter(adapter);
        medicineNameInput.setThreshold(2);
        requestQueue = Volley.newRequestQueue(this);

        medicineNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 1) {
                    Log.d(TAG, "Text changed: " + s.toString());
                    fetchMedicineSuggestions(s.toString());
                } else {
                    suggestions.clear();
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        medicineNameInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = adapter.getItem(position);
                Log.d(TAG, "Selected medicine: " + selected);
                fetchMedicineInfo(selected);
            }
        });

        searchButton.setOnClickListener(v -> {
            String text = medicineNameInput.getText().toString().trim();
            if (!text.isEmpty()) {
                Log.d(TAG, "Search button clicked, searching for: " + text);
                fetchMedicineInfo(text);
            } else {
                Toast.makeText(this, "Wpisz nazwę leku", Toast.LENGTH_SHORT).show();
            }
        });

        medicineNameInput.setOnEditorActionListener((v, actionId, event) -> {
            String text = medicineNameInput.getText().toString().trim();
            if (!text.isEmpty()) {
                Log.d(TAG, "Enter pressed, searching for: " + text);
                fetchMedicineInfo(text);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (toggle != null) {
            toggle.syncState();
            toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (toggle != null) {
            toggle.onConfigurationChanged(newConfig);
            toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));
        }
    }

    private void fetchMedicineSuggestions(String query) {
        String cleanQuery = query.trim().replaceAll("\\s+", " ");

        // Nowy URL dla Twojego API
        String url = API_BASE_URL + "/suggestions?search=" + Uri.encode(cleanQuery) + "&limit=10";
        Log.d(TAG, "Suggestions URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Suggestions response received");
                        suggestions.clear();
                        try {
                            JSONArray results = response.getJSONArray("results");
                            Log.d(TAG, "Found " + results.length() + " suggestions");

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject obj = results.getJSONObject(i);
                                JSONObject openfda = obj.optJSONObject("openfda");

                                if (openfda != null) {
                                    // Dodaj brand names
                                    if (openfda.has("brand_name")) {
                                        JSONArray names = openfda.getJSONArray("brand_name");
                                        for (int j = 0; j < names.length(); j++) {
                                            String name = names.getString(j);
                                            if (!suggestions.contains(name)) {
                                                suggestions.add(name);
                                                Log.d(TAG, "Added brand suggestion: " + name);
                                            }
                                        }
                                    }

                                    // Dodaj generic names
                                    if (openfda.has("generic_name")) {
                                        JSONArray names = openfda.getJSONArray("generic_name");
                                        for (int j = 0; j < names.length(); j++) {
                                            String name = names.getString(j);
                                            if (!suggestions.contains(name)) {
                                                suggestions.add(name);
                                                Log.d(TAG, "Added generic suggestion: " + name);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error for suggestions: " + e.getMessage());
                        }

                        Log.d(TAG, "Total suggestions: " + suggestions.size());
                        adapter.notifyDataSetChanged();

                        if (suggestions.size() > 0) {
                            runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                if (medicineNameInput.hasFocus()) {
                                    medicineNameInput.showDropDown();
                                    Log.d(TAG, "Dropdown shown with " + suggestions.size() + " items");
                                }
                            });
                        } else {
                            Log.d(TAG, "No suggestions to show");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            suggestions.clear();
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "No suggestions found (404)");
                            return;
                        }
                        Log.e(TAG, "Suggestions error: " + error.getMessage());
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Error code: " + error.networkResponse.statusCode);
                        }

                        // Pokaż Toast przy błędzie połączenia
                        runOnUiThread(() -> {
                            Toast.makeText(MedicineInfo.this, "Błąd połączenia z serwerem", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
        requestQueue.add(request);
    }

    private void fetchMedicineInfo(String name) {
        Log.d(TAG, "Fetching info for: " + name);
        medicineInfoText.setText("Wyszukiwanie informacji o leku: " + name + "...");

        String cleanName = name.trim().replaceAll("\\s+", " ");

        // Nowy URL dla Twojego API
        String url = API_BASE_URL + "/medicine/" + Uri.encode(cleanName);
        Log.d(TAG, "Medicine info URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Medicine info response received");
                        try {
                            JSONArray results = response.getJSONArray("results");
                            Log.d(TAG, "Found " + results.length() + " results for medicine info");

                            if (results.length() > 0) {
                                JSONObject obj = results.getJSONObject(0);
                                StringBuilder info = new StringBuilder();
                                info.append("INFORMACJE O LEKU\n");
                                info.append("==================\n\n");
                                info.append("Nazwa: ").append(name).append("\n\n");

                                // Składnik aktywny
                                JSONObject openfda = obj.optJSONObject("openfda");
                                if (openfda != null && openfda.has("generic_name")) {
                                    JSONArray genericNames = openfda.getJSONArray("generic_name");
                                    if (genericNames.length() > 0) {
                                        info.append("Składnik aktywny: ").append(genericNames.getString(0)).append("\n\n");
                                    }
                                }

                                // Zastosowanie
                                if (obj.has("indications_and_usage")) {
                                    JSONArray usage = obj.getJSONArray("indications_and_usage");
                                    String usageText = cleanText(usage.getString(0));
                                    info.append("Zastosowanie:\n").append(usageText).append("\n\n");
                                }

                                // Dawkowanie
                                if (obj.has("dosage_and_administration")) {
                                    JSONArray dosage = obj.getJSONArray("dosage_and_administration");
                                    String dosageText = cleanText(dosage.getString(0));
                                    info.append("Dawkowanie:\n").append(dosageText).append("\n\n");
                                }

                                // Ostrzeżenia
                                if (obj.has("warnings")) {
                                    JSONArray warnings = obj.getJSONArray("warnings");
                                    String warningsText = cleanText(warnings.getString(0));
                                    info.append("Ostrzeżenia:\n").append(warningsText).append("\n\n");
                                }

                                // Producent
                                if (openfda != null && openfda.has("manufacturer_name")) {
                                    JSONArray manufacturers = openfda.getJSONArray("manufacturer_name");
                                    if (manufacturers.length() > 0) {
                                        info.append("Źródło: ").append(manufacturers.getString(0)).append("\n");
                                    }
                                }

                                medicineInfoText.setText(info.toString());
                                Log.d(TAG, "Medicine info displayed successfully");
                            } else {
                                String message = "Brak informacji o leku: " + name + "\n\nSpróbuj wpisać pełną nazwę leku lub sprawdź pisownię.";
                                medicineInfoText.setText(message);
                                Log.d(TAG, "No results found for: " + name);
                            }
                        } catch (JSONException e) {
                            String errorMsg = "Błąd przetwarzania danych: " + e.getMessage();
                            medicineInfoText.setText(errorMsg);
                            Log.e(TAG, errorMsg, e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = "Błąd wyszukiwania leku: " + name;

                        if (error.networkResponse != null) {
                            if (error.networkResponse.statusCode == 404) {
                                errorMsg = "Nie znaleziono leku: " + name + "\n\nSpróbuj wpisać inną nazwę.";
                            } else {
                                errorMsg += "\nKod błędu: " + error.networkResponse.statusCode;
                            }
                            Log.e(TAG, "Error code: " + error.networkResponse.statusCode);
                        } else {
                            errorMsg += "\nSprawdź połączenie internetowe i upewnij się, że serwer API działa.";
                        }

                        medicineInfoText.setText(errorMsg);
                        Log.e(TAG, errorMsg);
                        Toast.makeText(MedicineInfo.this, "Błąd wyszukiwania", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(request);
    }

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]+>", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\\n+", "\n")
                .trim();
    }
}