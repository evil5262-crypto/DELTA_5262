package com.example.fastinjector;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private EditText textInput;
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences("FastInjectorPrefs", MODE_PRIVATE);
        
        textInput = findViewById(R.id.text_input);
        Button btnEnable = findViewById(R.id.btn_enable);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnShow = findViewById(R.id.btn_show);
        Button btnSpeed = findViewById(R.id.btn_speed);
        
        btnEnable.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        
        btnSave.setOnClickListener(v -> {
            String text = textInput.getText().toString();
            prefs.edit().putString("inject_text", text).apply();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        });
        
        btnShow.setOnClickListener(v -> {
            String saved = prefs.getString("inject_text", "");
            textInput.setText(saved);
        });
        
        btnSpeed.setOnClickListener(v -> {
            Toast.makeText(this, "Speed: Instant", Toast.LENGTH_SHORT).show();
        });
    }
}
