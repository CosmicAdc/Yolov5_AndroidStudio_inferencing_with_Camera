package com.example.proyectoalbatorres;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.example.proyectoalbatorres.R;


import com.example.proyectoalbatorres.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'proyectoalbatorres' library on application startup.
    static {
        System.loadLibrary("proyectoalbatorres");
    }
    private android.widget.Button botonAzul;
    private android.widget.Button botonVerde;

    private android.widget.Button botonRojo;

    private android.widget.Button botonGeneral;
    private android.widget.ImageView original, gris;
    private ActivityMainBinding binding;
    //Para la navegacion
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        //Para la navegacion
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        // Example of a call to a native method


        //Para abrir menu
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch ((String)item.getTitle()) {
                    case "Histograma":
                        showFragment(new FragHistograma());
                        break;
                    case "Reemplazo de pixel":
                        showFragment(new FragPixelReemplazo());
                        break;
                    case "Filtros":
                        showFragment(new FragFiltros());
                        break;
                    case "Efecto":
                        showFragment(new FragEfecto());
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
        showFragment(new FragHistograma());

    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'proyectoalbatorres' native library,
     * which is packaged with this application.
     */

}