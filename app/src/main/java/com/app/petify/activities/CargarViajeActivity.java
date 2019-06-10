package com.app.petify.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.app.petify.R;
import com.app.petify.models.Precio;
import com.app.petify.models.Viaje;
import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
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
    private LinearLayout mLLFechaReserva;
    private LinearLayout mLLHoraReserva;
    private LinearLayout mLLCantMascotas;
    private TextView mTarifa;

    private DatabaseReference mDatabase;
    private Map<String, String> mapUsers = new HashMap<>();
    private int sumMascotas;
    private Viaje viaje;
    private boolean viajeAcomp = false;
    public Precio precioCfg;

    ImageButton mButtonDia;
    ImageButton mButtonHora;
    final Calendar c = Calendar.getInstance();
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);
    EditText mDiaReserva;
    EditText mHoraReserva;
    final int hora = c.get(Calendar.HOUR_OF_DAY);
    final int minuto = c.get(Calendar.MINUTE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_viaje);
        mSolicitarViaje = findViewById(R.id.solicitar_viaje);
        mSolicitarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viaje = new Viaje();
                if (mDiaReserva.getText().toString().equals("") || mHoraReserva.getText().toString().equals("")) {
                    viaje.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                    viaje.reserva = false;
                } else {
                    viaje.fecha = mDiaReserva.getText() + " " + mHoraReserva.getText();
                    viaje.reserva = true;
                }
                if (LocalDateTime.now().isBefore(LocalDateTime.parse(viaje.fecha, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))) || !viaje.reserva) {
                    mLLFechaReserva.setBackgroundResource(0);
                    mLLHoraReserva.setBackgroundResource(0);
                    sumMascotas = Integer.parseInt(mCantidadMascotasP.getSelectedItem().toString()) + Integer.parseInt(mCantidadMascotasM.getSelectedItem().toString()) + Integer.parseInt(mCantidadMascotasG.getSelectedItem().toString());
                    if (sumMascotas <= 3 && sumMascotas > 0) {
                        mLLCantMascotas.setBackgroundResource(0);
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
                        viaje.precio = Double.parseDouble(mTarifa.getText().toString().substring(2));
                        mDatabase.child("viajes").child(viaje.id).setValue(viaje);
                        Intent i = new Intent(getBaseContext(), ViajeCursoActivity.class);
                        i.putExtra("VIAJE_ID", viaje.id);
                        i.putExtra("CHOFER_ID", viaje.chofer);
                        startActivity(i);
                    } else {
                        mLLCantMascotas.setBackground(getDrawable(R.drawable.bordered));
                    }
                } else {
                    mLLFechaReserva.setBackground(getDrawable(R.drawable.bordered));
                    mLLHoraReserva.setBackground(getDrawable(R.drawable.bordered));
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

        mLLFechaReserva = findViewById(R.id.linearlayout_fecha);
        mLLHoraReserva = findViewById(R.id.linearlayout_hora);
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
        mDatabase.child("conf").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                precioCfg = dataSnapshot.getValue(Precio.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mButtonDia = findViewById(R.id.cargar_pick_dia);
        mButtonDia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerFecha();
            }
        });
        mDiaReserva = findViewById(R.id.cargar_dia);
        mButtonHora = findViewById(R.id.cargar_pick_hora);
        mButtonHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerHora();
            }
        });
        mHoraReserva = findViewById(R.id.cargar_hora);
    }

    public void updateTarifa() {
        mTarifa.setText("$ " + tarifa().toString());
    }

    public Double tarifa() {
        try {
            Double total = 0.0;
            if (sumMascotas >= 0 && sumMascotas <= 3) {
                if (viajeAcomp)
                    total += Integer.parseInt(precioCfg.precioAcom);
                total += Integer.parseInt(precioCfg.precioMascota) * sumMascotas;
                total += Integer.parseInt(duration.split(" ")[0]) * Integer.parseInt(precioCfg.precioMinuto);
                total += Float.parseFloat(distance.split(" ")[0]) * Integer.parseInt(precioCfg.precioKm);
                int hour = LocalDateTime.now().getHour();
                if (hour >= Integer.parseInt(precioCfg.inicioHN) || hour < Integer.parseInt(precioCfg.finHN))
                    total *= Float.parseFloat(precioCfg.multiplicadorHN);
                return total;
            }
            return 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private void obtenerFecha() {
        DatePickerDialog recogerFecha = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final int mesActual = month + 1;
                String diaFormateado = (dayOfMonth < 10) ? "0" + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
                String mesFormateado = (mesActual < 10) ? "0" + String.valueOf(mesActual) : String.valueOf(mesActual);
                mDiaReserva.setText(year + "/" + mesFormateado + "/" + diaFormateado);
            }
        }, anio, mes, dia);
        recogerFecha.getDatePicker().setMinDate(System.currentTimeMillis());
        recogerFecha.show();
    }

    private void obtenerHora() {
        TimePickerDialog recogerHora = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String horaFormateada = (hourOfDay < 10) ? "0" + String.valueOf(hourOfDay) : String.valueOf(hourOfDay);
                String minutoFormateado = (minute < 10) ? "0" + String.valueOf(minute) : String.valueOf(minute);
                mHoraReserva.setText(horaFormateada + ":" + minutoFormateado);
            }
        }, hora, minuto, true);
        recogerHora.show();
    }
}
