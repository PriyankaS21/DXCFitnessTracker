package com.example.dxcfitnesstracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.activity_main);
        setContentView(R.layout.profile_layout);
    }
    @Override
    public boolean onSupportNavigateUp(){
        return Navigation.findNavController(this,R.id.nav_host_fragment).navigateUp();
    }
}
