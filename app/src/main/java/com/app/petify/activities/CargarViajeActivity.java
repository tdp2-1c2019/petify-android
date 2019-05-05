package com.app.petify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.app.petify.R;
import com.app.petify.models.Viaje;
import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CargarViajeActivity extends AppCompatActivity {

    private Spinner mCantidadMascotasP;
    private Spinner mCantidadMascotasM;
    private Spinner mCantidadMascotasG;
    private Switch mViajaAcompanante;
    private Spinner mFormaPago;
    private EditText mObservaciones;
    private Button mSolicitarViaje;

    private String origin_address;
    private Double origin_latitude;
    private Double origin_longitude;
    private String destination_address;
    private Double destination_latitude;
    private Double destination_longitude;
    private String duration;
    private String distance;
    private LinearLayout mLLCantMascotas;
    private TextView mTarifa;

    private DatabaseReference mDatabase;
    private Map<String, String> mapUsers = new HashMap<>();
    private int sumMascotas;
    private Viaje viaje;
    private boolean viajeAcomp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_viaje);
        mSolicitarViaje = findViewById(R.id.solicitar_viaje);
        mSolicitarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sumMascotas = Integer.parseInt(mCantidadMascotasP.getSelectedItem().toString()) + Integer.parseInt(mCantidadMascotasM.getSelectedItem().toString()) + Integer.parseInt(mCantidadMascotasG.getSelectedItem().toString());
                if (sumMascotas <= 3 && sumMascotas > 0) {
                    mLLCantMascotas.setBackgroundResource(0);
                    viaje = new Viaje();
                    viaje.id = UUID.randomUUID().toString();
                    viaje.pasajero = Profile.getCurrentProfile().getId();
                    viaje.estado = Viaje.CARGADO; // TODO en chofer se asigna despues
                    viaje.origin_address = origin_address;
                    viaje.origin_latitude = origin_latitude;
                    viaje.origin_longitude = origin_longitude;
                    viaje.destination_address = destination_address;
                    viaje.destination_latitude = destination_latitude;
                    viaje.destination_longitude = destination_longitude;
                    viaje.cantidadMascotas = String.valueOf(sumMascotas);
                    viaje.viajaAcompanante = mViajaAcompanante.isChecked();
                    viaje.formaPago = mFormaPago.getSelectedItem().toString();
                    viaje.observaciones = mObservaciones.getText().toString();
                    mDatabase.child("viajes").child(viaje.id).setValue(viaje);
                    Intent i = new Intent(getBaseContext(), ViajeCursoActivity.class);
                    i.putExtra("VIAJE_ID", viaje.id);
                    i.putExtra("CHOFER_ID", viaje.chofer);
                    startActivity(i);
                } else {
                    mLLCantMascotas.setBackground(getDrawable(R.drawable.bordered));
                }
            }
        });

        Intent intent = getIntent();
        origin_address = intent.getStringExtra("ORIGIN_ADDRESS");
        origin_latitude = intent.getDoubleExtra("ORIGIN_LAT", 0);
        origin_longitude = intent.getDoubleExtra("ORIGIN_LNG", 0);
        destination_address = intent.getStringExtra("DESTINATION_ADDRESS");
        destination_latitude = intent.getDoubleExtra("DESTINATION_LAT", 0);
        destination_longitude = intent.getDoubleExtra("DESTINATION_LNG", 0);
        duration = intent.getStringExtra("DURATION");
        distance = intent.getStringExtra("DISTANCE");

        TextView mOriginText = findViewById(R.id.origin_text);
        mOriginText.setText("Origen: " + origin_address);
        TextView mDestinationText = findViewById(R.id.destination_text);
        mDestinationText.setText("Destino: " + destination_address);
        TextView mDurationText = findViewById(R.id.duration_text);
        mDurationText.setText("Tiempo estimado: " + duration);
        TextView mDistanceText = findViewById(R.id.distance_text);
        mDistanceText.setText("Distancia estimada: " + distance);

        mLLCantMascotas = findViewById(R.id.linearlayout_cant_mascotas);
        mCantidadMascotasP = findViewById(R.id.cantidad_mascotas_p);
        mCantidadMascotasP.setSelection(0);
        mCantidadMascotasP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sumMascotas = Integer.parseInt(mCantidadMascotasP.getSelectedItem().toString()) + Integer.parseInt(mCantidadMascotasM.getSelectedItem().toString()) + Integer.parseInt(mCantidadMascotasG.getSelectedItem().toString());
                updateTarifa();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mCantidadMascotasM = findViewById(R.id.cantidad_mascotas_m);
        mCantidadMascotasM.setSelection(0);
        mCantidadMascotasM.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sumMascotas = Integer.parseInt(mCantidadMascotasP.getSelectedItem().toString()) + Integer.parseInt(mCantidadMascotasM.getSelectedItem().toString()) + Integer.parseInt(mCantidadMascotasG.getSelectedItem().toString());
                updateTarifa();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mCantidadMascotasG = findViewById(R.id.cantidad_mascotas_g);
        mCantidadMascotasG.setSelection(0);
        mCantidadMascotasG.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sumMascotas = Integer.parseInt(mCantidadMascotasP.getSelectedItem().toString()) + Integer.parseInt(mCantidadMascotasM.getSelectedItem().toString()) + Integer.parseInt(mCantidadMascotasG.getSelectedItem().toString());
                updateTarifa();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mViajaAcompanante = findViewById(R.id.viaja_acompanante);
        mViajaAcompanante.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viajeAcomp = isChecked;
                updateTarifa();
            }
        });
        mFormaPago = findViewById(R.id.forma_pago);
        mFormaPago.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTarifa();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mObservaciones = findViewById(R.id.observaciones);

        mTarifa = findViewById(R.id.precio_estimado_text);

        mDatabase = FirebaseDatabase.getInstance().getReference();
//        mDatabase.child("drivers").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot d : dataSnapshot.getChildren()) {
//                    if ((Boolean) d.child("disponible").getValue()) {
//                        mapUsers.put((String) d.child("name").getValue(), d.getKey());
//                    }
//                }
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_layout, mapUsers.keySet().toArray(new String[mapUsers.size()]));
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                mChofer.setAdapter(adapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
    }

    public void updateTarifa() {
        mTarifa.setText(tarifa());
    }

    public String tarifa() {
        try {
            float total = 0;
            if (sumMascotas > 0 && sumMascotas <= 3) {
                if (viajeAcomp)
                    total += 100;
                total += 50 * sumMascotas;
                total += Integer.parseInt(duration.split(" ")[0]) * 5;
                total += Float.parseFloat(distance.split(" ")[0]) * 25;
                int hour = LocalDateTime.now().getHour();
                if (hour >= 18 || hour < 6)
                    total *= 1.5;
                return "$ " + String.format("%.02f", total);
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
}
