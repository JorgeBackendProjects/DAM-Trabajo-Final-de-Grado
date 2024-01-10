package com.example.a14kapp;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;

import com.example.a14kapp.databinding.ActivityInicioBinding;

public class Inicio extends AppCompatActivity {

    private ActivityInicioBinding binding;
    private String email;
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getString("email");
        }

        binding = ActivityInicioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new ProductsFragment(email));

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.navigation_products:
                    replaceFragment(new ProductsFragment(email));
                    break;
                case R.id.navigation_cita:
                    replaceFragment(new CitaFragment(email));
                    break;
                case R.id.navigation_reservas:
                    replaceFragment(new ReservasFragment(email));
                    break;
                case R.id.navigation_favoritos:
                    replaceFragment(new FavoritosFragment(email));
                    break;
                case R.id.navigation_configuracion:
                    replaceFragment(new ConfiguracionFragment(email));
                    break;
            }

            return true;
        });

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Deseas cerrar la sesión?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}