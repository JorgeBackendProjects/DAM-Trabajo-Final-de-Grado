package com.example.a14kapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.a14kapp.Entidades.Cita;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ModificarReserva extends AppCompatActivity {

    private EditText descripcionI, fechaI;
    private Spinner horaI, tiendaI;
    private String [] tiendas;
    private Button guardar;
    private Bundle bundle;
    private View view;
    private String email, descripcion, mes, mesA, dia, diaA, hora, horaA, tienda;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_reserva);

        bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getString("Email");
            descripcion = bundle.getString("Descripcion");
            mes = bundle.getString("Mes");
            dia = bundle.getString("Dia");
            hora = bundle.getString("Hora");
            tienda = bundle.getString("Tienda");
        }

        mesA = mes;
        diaA = dia;
        horaA = hora;

        descripcionI = findViewById(R.id.descripcionIMR);
        fechaI = findViewById(R.id.fechaIMR);
        horaI = findViewById(R.id.selectorHora_2);
        tiendaI = findViewById(R.id.selectorTienda_2);

        descripcionI.setText(descripcion);

        setUpHoraSpinner();

        tiendas = getResources().getStringArray(R.array.tiendas);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ModificarReserva.this,
                R.array.tiendas, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tiendaI.setAdapter(adapter);

        //DEBO GUARDAR LA PRIMERA FECHA Y HORA PARA PONERLA A FALSE...

        guardar = findViewById(R.id.modificarReservaB);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ModificarReserva.this);
                builder.setTitle("Confirmar Modificación")
                        .setMessage("¿Estás seguro de que deseas modificar esta cita?")
                        .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reservaNueva();
                                eliminarReservaAntigua();
                                ocuparNuevaReserva();
                                reactivarReservaAntigua();

                                //ModificarReserva.super.onBackPressed();
                                Intent intent = new Intent(ModificarReserva.this, Inicio.class);
                                intent.putExtra("email", email);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void reservaNueva(){
        DatabaseReference clienteRef = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("cliente");
        Query query = clienteRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot clienteSnapshot = dataSnapshot.getChildren().iterator().next();
                    DatabaseReference clienteRef = clienteSnapshot.getRef();
                    DatabaseReference citasRef = clienteRef.child("citas").child(mesA).child(diaA).child(horaA);

                    citasRef.removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Crear una nueva cita con los nuevos datos
                                    DatabaseReference nuevasCitasRef = clienteRef.child("citas").push();
                                    nuevasCitasRef.setValue(new Cita(mes, dia, horaI.getSelectedItem().toString(), descripcionI.getText().toString(), tiendaI.getSelectedItem().toString()))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // La cita se modificó correctamente
                                                    Toast.makeText(getApplicationContext(), "La cita se modificó correctamente", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Error al modificar la cita
                                                    Toast.makeText(getApplicationContext(), "Error al modificar la cita", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {}
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }

    private void ocuparNuevaReserva(){
        DatabaseReference citasRef = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("citas")
                .child(mes).child(dia).child(horaI.getSelectedItem().toString());

        citasRef.setValue(false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {}
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {}
                });
    }

    private void eliminarReservaAntigua() {
        DatabaseReference clienteRef = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("cliente");
        Query query = clienteRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot clienteSnapshot = dataSnapshot.getChildren().iterator().next();
                    DatabaseReference clienteRef = clienteSnapshot.getRef();
                    DatabaseReference citasRef = clienteRef.child("citas");

                    citasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot citaSnapshot : dataSnapshot.getChildren()) {
                                    String mes = citaSnapshot.child("mes").getValue(String.class);
                                    String dia = citaSnapshot.child("dia").getValue(String.class);
                                    String hora = citaSnapshot.child("hora").getValue(String.class);

                                    if (mes.equals(mesA) && dia.equals(diaA) && hora.equals(horaA)) {
                                        String citaKey = citaSnapshot.getKey();
                                        DatabaseReference citaRef = citasRef.child(citaKey);

                                        citaRef.removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {}
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {}
                                                });

                                        citasRef.removeEventListener(this);
                                        return;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


    private void reactivarReservaAntigua() {
        DatabaseReference citasRef = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("citas").child(mesA).child(diaA).child(horaA);

        citasRef.setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {}
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {}
                });
    }

    private void setUpHoraSpinner() {
        ArrayList<String> horaList = new ArrayList<>();
        ArrayAdapter horaAdapter = new ArrayAdapter<>(ModificarReserva.this, android.R.layout.simple_spinner_item, horaList);
        horaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        horaI.setAdapter(horaAdapter);

        fechaI.addTextChangedListener(new TextWatcher() {

            String fecha = fechaI.getText().toString();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String fecha = s.toString();

                if (fecha.isEmpty()) {
                    // El campo de fecha está vacío, vaciar el spinner de horas
                    horaList.clear();
                    horaAdapter.notifyDataSetChanged();
                    return;
                }

                if (fecha.length() < 4) {
                    return;
                }

                String[] fechaSeleccionada = fecha.split("/");
                mes = fechaSeleccionada[1];
                dia = fechaSeleccionada[0];

                mes = convertirMes(mes);
                dia = convertirDia(dia);

                DatabaseReference citasRef = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("citas");
                citasRef.child(mes).child(dia).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> horas = new ArrayList<>();

                        for (DataSnapshot horaSnapshot : snapshot.getChildren()) {
                            Boolean disponible = horaSnapshot.getValue(Boolean.class);
                            if (disponible) {
                                horas.add(horaSnapshot.getKey());
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ModificarReserva.this, android.R.layout.simple_spinner_item, horas);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        horaI.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        String fecha = fechaI.getText().toString().trim();
        if (fecha.length() < 3) {
            horaList.clear();
            horaAdapter.notifyDataSetChanged();
        }

    }

    public String convertirMes(String mes){

        switch (mes) {
            case "1":
                mes = "Enero";
                break;
            case "01":
                mes = "Enero";
                break;
            case "2":
                mes = "Febrero";
                break;
            case "02":
                mes = "Febrero";
                break;
            case "3":
                mes = "Marzo";
                break;
            case "03":
                mes = "Marzo";
                break;
            case "4":
                mes = "Abril";
                break;
            case "04":
                mes = "Abril";
                break;
            case "5":
                mes = "Mayo";
                break;
            case "05":
                mes = "Mayo";
                break;
            case "6":
                mes = "Junio";
                break;
            case "06":
                mes = "Junio";
                break;
            case "7":
                mes = "Julio";
                break;
            case "07":
                mes = "Julio";
                break;
            case "8":
                mes = "Agosto";
                break;
            case "08":
                mes = "Agosto";
                break;
            case "9":
                mes = "Septiembre";
                break;
            case "09":
                mes = "Septiembre";
                break;
            case "10":
                mes = "Octubre";
                break;
            case "11":
                mes = "Noviembre";
                break;
            case "12":
                mes = "Diciembre";
                break;
        }

        return mes;
    }

    public String convertirMes2(String mes){
        switch (mes) {
            case "Enero":
                mes = "01";
                break;
            case "Febrero":
                mes = "02";
                break;
            case "Marzo":
                mes = "03";
                break;
            case "Abril":
                mes = "04";
                break;
            case "Mayo":
                mes = "05";
                break;
            case "Junio":
                mes = "06";
                break;
            case "Julio":
                mes = "07";
                break;
            case "Agosto":
                mes = "08";
                break;
            case "Septiembre":
                mes = "09";
                break;
            case "Octubre":
                mes = "10";
                break;
            case "Noviembre":
                mes = "11";
                break;
            case "Diciembre":
                mes = "12";
                break;
            case "7":
                mes = "Julio";
                break;
            case "07":
                mes = "Julio";
                break;
            case "8":
                mes = "Agosto";
                break;
            case "08":
                mes = "Agosto";
                break;
            case "9":
                mes = "Septiembre";
                break;
            case "09":
                mes = "Septiembre";
                break;
            case "10":
                mes = "Octubre";
                break;
            case "11":
                mes = "Noviembre";
                break;
            case "12":
                mes = "Diciembre";
                break;
        }

        return mes;
    }

    public String convertirDia(String dia){
        switch (dia) {
            case "01":
                dia = "1";
                break;
            case "02":
                dia = "2";
                break;
            case "03":
                dia = "3";
                break;
            case "04":
                dia = "4";
                break;
            case "05":
                dia = "5";
                break;
            case "06":
                dia = "6";
                break;
            case "07":
                dia = "7";
                break;
            case "08":
                dia = "8";
                break;
            case "09":
                dia = "9";
                break;
        }

        return dia;
    }
}