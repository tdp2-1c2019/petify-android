package com.app.petify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.widget.TextView;

import com.app.petify.R;
import com.app.petify.models.Usuario;
import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PerfilActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navView;
    private TextView drawerTitle;
    private MenuItem itemPerfil;
    private TextView nombre;
    private TextView correo;
    private TextView direccion;
    private TextView tel;
    private TextView rol;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        navView = findViewById(R.id.driver_nav_view);
        navView.setNavigationItemSelectedListener(this);
        itemPerfil = navView.getMenu().getItem(2);
        drawerTitle = navView.getHeaderView(0).findViewById(R.id.nav_header_title);
        drawerTitle.setText("Hola " + Profile.getCurrentProfile().getFirstName() + "!");
        SpannableString s = new SpannableString(itemPerfil.getTitle());
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.com_facebook_blue)), 0, s.length(), 0);
        itemPerfil.setTitle(s);

        nombre = findViewById(R.id.profile_name);
        correo = findViewById(R.id.profile_mail);
        direccion = findViewById(R.id.profile_address);
        tel = findViewById(R.id.profile_tel);
        rol = findViewById(R.id.profile_rol);

        mDatabase.child("customers").child(Profile.getCurrentProfile().getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario u = dataSnapshot.getValue(Usuario.class);
                if (u != null) {
                    nombre.setText(u.name);
                    correo.setText(u.email);
                    direccion.setText(u.direccion);
                    tel.setText(u.telefono);
                    rol.setText("Pasajero");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.child("drivers").child(Profile.getCurrentProfile().getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario u = dataSnapshot.getValue(Usuario.class);
                if (u != null) {
                    nombre.setText(u.name);
                    correo.setText(u.email);
                    direccion.setText(u.direccion);
                    tel.setText(u.telefono);
                    rol.setText("Conductor");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Intent i = null;
        if (id == R.id.nav_item_inicio)
            i = new Intent(this, DriverHomeActivity.class);
        if (id == R.id.nav_item_viajes)
            i = new Intent(this, MyTrips.class);
        startActivity(i);
        return false;
    }
}
