package com.example.healtwatchp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healtwatchp.adapter.SymptomAdapter;
import com.example.healtwatchp.models.Symptom;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;

public class SymptomsListActivity extends AppCompatActivity implements SymptomAdapter.OnSymptomClickListener {

    private RecyclerView recyclerViewSymptoms;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewEmpty, textViewError;
    private FloatingActionButton fabAddSymptom;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private SymptomAdapter adapter;
    private List<Symptom> symptoms;
    private String apiKey;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(true);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_symptoms) {
                // Already on symptoms page, just close drawer
            } else if (id == R.id.nav_medications) {
                Intent intent = new Intent(SymptomsListActivity.this, MainBoard.class);
                intent.putExtra("apiKey", apiKey);
                startActivity(intent);
            } else if (id == R.id.nav_appointments) {
                Intent intent = new Intent(SymptomsListActivity.this, AppointmentsBoard.class);
                intent.putExtra("apiKey", apiKey);
                startActivity(intent);
            } else if (id == R.id.nav_medicine_info) {
                Intent intent = new Intent(SymptomsListActivity.this, MedicineInfo.class);
                intent.putExtra("apiKey", apiKey);
                startActivity(intent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        initViews();
        setupRecyclerView();

        sharedPreferences = getSharedPreferences("MyAppName", MODE_PRIVATE);
        apiKey = sharedPreferences.getString("apiKey", "");

        loadSymptoms();

        fabAddSymptom.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddSymptomActivity.class);
            startActivity(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(this::loadSymptoms);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSymptoms(); // Odśwież listę gdy wracamy do aktywności
    }

    private void initViews() {
        recyclerViewSymptoms = findViewById(R.id.recyclerViewSymptoms);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        textViewError = findViewById(R.id.textViewError);
        fabAddSymptom = findViewById(R.id.fab_add_symptom); // fixed ID
    }

    private void setupRecyclerView() {
        symptoms = new ArrayList<>();
        adapter = new SymptomAdapter(this, symptoms);
        adapter.setOnSymptomClickListener(this);

        recyclerViewSymptoms.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSymptoms.setAdapter(adapter);
    }

    private void loadSymptoms() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        textViewError.setVisibility(View.GONE);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8080/api/symptoms/list?apiKey=" + apiKey;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");

                            if (status.equals("success")) {
                                JSONArray symptomsArray = jsonObject.getJSONArray("symptoms");
                                symptoms.clear();

                                for (int i = 0; i < symptomsArray.length(); i++) {
                                    JSONObject symptomObj = symptomsArray.getJSONObject(i);

                                    Symptom symptom = new Symptom();
                                    symptom.setId(symptomObj.getLong("id"));
                                    symptom.setSymptomName(symptomObj.getString("symptomName"));
                                    symptom.setIntensity(symptomObj.getInt("intensity"));
                                    symptom.setSymptomDate(symptomObj.getString("symptomDate"));

                                    if (!symptomObj.isNull("notes")) {
                                        symptom.setNotes(symptomObj.getString("notes"));
                                    }

                                    symptoms.add(symptom);
                                }

                                adapter.notifyDataSetChanged();
                                updateEmptyView();

                            } else {
                                String message = jsonObject.getString("message");
                                textViewError.setText(message);
                                textViewError.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            Log.e("JSONError", "Błąd parsowania JSON", e);
                            textViewError.setText("Błąd wczytywania danych");
                            textViewError.setVisibility(View.VISIBLE);
                        }

                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textViewError.setText("Błąd połączenia! Sprawdź internet.");
                textViewError.setVisibility(View.VISIBLE);
                Log.e("VolleyError", "Error: " + error.getMessage());

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        queue.add(stringRequest);
    }

    private void updateEmptyView() {
        if (symptoms.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewSymptoms.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewSymptoms.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteClick(Symptom symptom, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Usuń objaw")
                .setMessage("Czy na pewno chcesz usunąć objaw \"" + symptom.getSymptomName() + "\"?")
                .setPositiveButton("Usuń", (dialog, which) -> deleteSymptom(symptom, position))
                .setNegativeButton("Anuluj", null)
                .show();
    }

    @Override
    public void onItemClick(Symptom symptom) {
        // Tutaj możesz dodać szczegóły objawu lub edycję
        Toast.makeText(this,
                symptom.getSymptomName() + " - " + symptom.getIntensityDescription(),
                Toast.LENGTH_SHORT).show();
    }

    private void deleteSymptom(Symptom symptom, int position) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8080/api/symptoms/delete";

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if (status.equals("success")) {
                                adapter.removeItem(position);
                                updateEmptyView();
                                Toast.makeText(SymptomsListActivity.this, "Objaw został usunięty", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SymptomsListActivity.this, message, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Log.e("JSONError", "Błąd parsowania JSON", e);
                            Toast.makeText(SymptomsListActivity.this, "Błąd podczas usuwania", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SymptomsListActivity.this, "Błąd połączenia!", Toast.LENGTH_SHORT).show();
                Log.e("VolleyError", "Error: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("apiKey", apiKey);
                params.put("symptomId", String.valueOf(symptom.getId()));
                return params;
            }
        };

        queue.add(stringRequest);
    }
}