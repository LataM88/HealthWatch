package com.example.healtwatchp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.healtwatchp.adapter.Medication;
import com.example.healtwatchp.adapter.MedicationAdapter;
import com.example.healtwatchp.notifications.NotificationHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainBoard extends AppCompatActivity implements MedicationAdapter.OnDeleteClickListener {

    EditText nameText, dosageText;
    Button timeButton, addButton, cancelButton;
    TextView textViewError;
    String selectedTime = "";
    String apiKey = "";
    FloatingActionButton fabAdd;
    CardView bottomPanel;
    ChipGroup dayChipGroup;

    RecyclerView recyclerView;
    MedicationAdapter adapter;
    ArrayList<Medication> medicationList;
    ArrayList<Medication> allMedications = new ArrayList<>();

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_medications) {
                    // Already in Medications section
                } else if (id == R.id.nav_appointments) {
                    Intent intent = new Intent(MainBoard.this, AppointmentsBoard.class);
                    intent.putExtra("apiKey", apiKey);
                    startActivity(intent);
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        initViews();
        setupListeners();
        setupRecyclerView();

        apiKey = getIntent().getStringExtra("apiKey");
        Log.d("MainBoard", "Otrzymano API Key: " + apiKey);

        bottomPanel.setVisibility(View.GONE);

        fetchMedications();
    }

    private void initViews() {
        nameText = findViewById(R.id.name);
        dosageText = findViewById(R.id.dosage);
        timeButton = findViewById(R.id.select_time);
        addButton = findViewById(R.id.add);
        cancelButton = findViewById(R.id.cancel_add);
        textViewError = findViewById(R.id.error_message);
        fabAdd = findViewById(R.id.fab_add);
        bottomPanel = findViewById(R.id.bottom_panel);
        dayChipGroup = findViewById(R.id.day_chip_group);
        recyclerView = findViewById(R.id.recycler);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        medicationList = new ArrayList<>();
        adapter = new MedicationAdapter(medicationList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
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
            String days = getSelectedDays();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dosage) || TextUtils.isEmpty(selectedTime) || TextUtils.isEmpty(days)) {
                textViewError.setText("Wszystkie pola muszą być wypełnione!");
                textViewError.setVisibility(View.VISIBLE);
                return;
            } else {
                textViewError.setVisibility(View.GONE);
                sendMedicationToServer(name, dosage, selectedTime, days);
                // Integracja z NotificationHelper
                Medication medication = new Medication(-1L, name, dosage, selectedTime, days);
                NotificationHelper notificationHelper = new NotificationHelper(MainBoard.this);
                notificationHelper.scheduleMedicationNotification(medication);
            }
        });

        setupDayButtons();
    }

    private void setupDayButtons() {
        dayChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                adapter.updateData(new ArrayList<>(allMedications));
                return;
            }

            Chip selectedChip = findViewById(checkedId);
            if (selectedChip != null) {
                filterMedicationsByDay(selectedChip.getText().toString());
            }
        });
    }

    private void clearForm() {
        nameText.setText("");
        dosageText.setText("");
        timeButton.setText("Wybierz godzinę");
        selectedTime = "";

        ((CheckBox) findViewById(R.id.checkbox_monday)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_tuesday)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_wednesday)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_thursday)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_friday)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_saturday)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_sunday)).setChecked(false);

        textViewError.setVisibility(View.GONE);
    }

    private String getSelectedDays() {
        ArrayList<String> selectedDays = new ArrayList<>();
        if (((CheckBox) findViewById(R.id.checkbox_monday)).isChecked()) selectedDays.add("Pon");
        if (((CheckBox) findViewById(R.id.checkbox_tuesday)).isChecked()) selectedDays.add("Wt");
        if (((CheckBox) findViewById(R.id.checkbox_wednesday)).isChecked()) selectedDays.add("Śr");
        if (((CheckBox) findViewById(R.id.checkbox_thursday)).isChecked()) selectedDays.add("Czw");
        if (((CheckBox) findViewById(R.id.checkbox_friday)).isChecked()) selectedDays.add("Pt");
        if (((CheckBox) findViewById(R.id.checkbox_saturday)).isChecked()) selectedDays.add("Sob");
        if (((CheckBox) findViewById(R.id.checkbox_sunday)).isChecked()) selectedDays.add("Ndz");
        return TextUtils.join(", ", selectedDays);
    }

    private void fetchMedications() {
        String url = "http://10.0.2.2:8080/api/medications";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("FetchMedications", "Cała odpowiedź API: " + response.toString());

                        medicationList.clear();
                        allMedications.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            Log.d("FetchMedications", "Obiekt JSON: " + jsonObject.toString());

                            long id = -1;
                            try {
                                id = jsonObject.getLong("id");
                                if (id <= 0) {
                                    Log.e("FetchMedications", "Brak poprawnego ID w obiekcie: " + jsonObject.toString());
                                    id = -1;
                                }
                            } catch (Exception e) {
                                Log.e("FetchMedications", "Błąd podczas parsowania ID: " + jsonObject.toString(), e);
                            }

                            if (id != -1) {
                                Medication medication = new Medication(
                                        id,
                                        jsonObject.getString("name"),
                                        jsonObject.getString("dosage"),
                                        jsonObject.getString("time"),
                                        jsonObject.getString("days")
                                );
                                medicationList.add(medication);
                                allMedications.add(medication);
                            }
                        }
                        adapter.updateData(new ArrayList<>(medicationList));
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


    private void filterMedicationsByDay(String selectedDay) {
        if (selectedDay == null) {
            adapter.updateData(new ArrayList<>(allMedications));
            return;
        }

        ArrayList<Medication> filteredList = new ArrayList<>();
        for (Medication medication : allMedications) {
            if (medication.getDays().contains(selectedDay)) {
                filteredList.add(medication);
            }
        }
        adapter.updateData(filteredList);
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
                    bottomPanel.setVisibility(View.GONE);
                    fabAdd.show();
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

    @Override
    public void onDeleteClick(int position, Medication medication) {
        Log.d("DELETE", "Próbuję usunąć lek: " + medication.getName() + ", ID: " + medication.getId());
        deleteMedicationFromServer(medication, position);
    }

    private void deleteMedicationFromServer(Medication medication, int position) {
        if (medication.getId() == null) {
            Toast.makeText(MainBoard.this, "Nie można usunąć leku bez ID!", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8080/api/medication/" + medication.getId();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", medication.getName());
            jsonBody.put("dosage", medication.getDosage());
            jsonBody.put("time", medication.getTime());
            jsonBody.put("days", medication.getDays());
        } catch (JSONException e) {
            Toast.makeText(MainBoard.this, "Błąd tworzenia danych JSON!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    // Usunięcie z lokalnej listy
                    adapter.removeItem(position);

                    // Usunięcie z allMedications
                    for (int i = 0; i < allMedications.size(); i++) {
                        Medication med = allMedications.get(i);
                        if (med.getName().equals(medication.getName()) &&
                                med.getDosage().equals(medication.getDosage()) &&
                                med.getTime().equals(medication.getTime())) {
                            allMedications.remove(i);
                            break;
                        }
                    }

                    Toast.makeText(MainBoard.this, "Lek został usunięty!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(MainBoard.this, "Błąd usuwania leku!", Toast.LENGTH_SHORT).show();
                    Log.e("DeleteMedication", "Błąd: " + error.toString());
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.d("DELETE", "Wysyłanie API Key: " + apiKey);
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", apiKey);
                return headers;
            }
        };
        queue.add(deleteRequest);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}