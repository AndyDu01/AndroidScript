package com.example.androidscript;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ClickerActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clicker);

    }

    public void clicktester(View view){
        Toast.makeText(this,"Autoclicksucess", Toast.LENGTH_SHORT).show();
    }

    public void Autoclick_swipe(View view){
        AutoClick.autoClickPos(540, 1310, 545, 1315);
    }
}
