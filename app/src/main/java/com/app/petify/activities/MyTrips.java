package com.app.petify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.app.petify.R;
import com.app.petify.models.Viaje;
import com.app.petify.models.ViajeAdapter;
import com.app.petify.utils.RecyclerItemClickListener;
import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MyTrips extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    RecyclerView rv;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    private NavigationView navView;
    private TextView drawerTitle;
    private MenuItem itemPerfil;
    private DatabaseReference mDatabase;
    FloatingActionButton goTopButton;
    List<Viaje> viajes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);
        rv = findViewById(R.id.rv_viajes);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(), DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(dividerItemDecoration);
        goTopButton = findViewById(R.id.rv_gotop);
        navView = findViewById(R.id.driver_nav_view);
        navView.setNavigationItemSelectedListener(this);
        itemPerfil = navView.getMenu().getItem(1);
        drawerTitle = navView.getHeaderView(0).findViewById(R.id.nav_header_title);
        drawerTitle.setText("Hola " + Profile.getCurrentProfile().getFirstName() + "!");
        SpannableString s = new SpannableString(itemPerfil.getTitle());
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.com_facebook_blue)), 0, s.length(), 0);
        itemPerfil.setTitle(s);

        mDatabase.child("viajes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Viaje> vs = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Viaje v = ds.getValue(Viaje.class);
                    if (v != null)
                        vs.add(v);
                }
                viajes = vs.stream()
                        .filter((v) ->
                                (v.chofer != null && v.chofer.equals(Profile.getCurrentProfile().getId())) ||
                                        (v.pasajero != null && v.pasajero.equals(Profile.getCurrentProfile().getId())))
                        .sorted(new Comparator<Viaje>() {
                            @Override
                            public int compare(Viaje o1, Viaje o2) {
                                return o2.fecha.compareTo(o1.fecha);
                            }
                        })
                        .collect(Collectors.toList());
                adapter = new ViajeAdapter(vs, getBaseContext());
                rv.setAdapter(adapter);
                rv.addOnItemTouchListener(new RecyclerItemClickListener(getBaseContext(), rv, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent i = new Intent(MyTrips.this, TripSummaryActivity.class);
                        i.putExtra("viaje", viajes.get(position));
                        startActivity(i);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                }));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        goTopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv.scrollToPosition(0);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Intent i = null;
        if (id == R.id.nav_item_inicio)
            i = new Intent(this, DriverHomeActivity.class);
        if (id == R.id.nav_item_perfil)
            i = new Intent(this, PerfilActivity.class);
        startActivity(i);
        return false;
    }
}
