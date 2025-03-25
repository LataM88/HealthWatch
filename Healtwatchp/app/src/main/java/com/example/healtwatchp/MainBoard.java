package com.example.healtwatchp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainBoard extends AppCompatActivity {

    EditText nameText;
    Button add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainboard);
        nameText = findViewById(R.id.name);
        add = findViewById(R.id.add);

        add.setOnClickListener(v->{
            if (TextUtils.isEmpty(nameText.getText())){
                Toast.makeText(MainBoard.this, "Podaj nazwe leku", Toast.LENGTH_SHORT);
            } else {

            }
        });
    }
}