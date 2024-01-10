package com.example.a14kapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.a14kapp.Entidades.Cita;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CitaFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CitaFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private FirebaseDatabase database;
    private DatabaseReference nDatabase;
    private FirebaseAuth nAuth;
    private View view;

    private static String email, mes, dia;
    private Spinner spinnerTienda, spinnerHora;
    private String [] tiendas;
    private EditText fechaI, descripcionI;

    public CitaFragment(String email) {
        // Required empty public constructor
        this.email = email;
    }

    public static CitaFragment newInstance(String param1, String param2) {
        CitaFragment fragment = new CitaFragment(email);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_cita, container, false);

        fechaI = view.findViewById(R.id.fechaI4);
        spinnerHora = view.findViewById(R.id.horaList);
        descripcionI = view.findViewById(R.id.descripcionIC);

        //SELECCIÓN DE TIENDA
        spinnerTienda = view.findViewById(R.id.tiendaList4);
        tiendas = getResources().getStringArray(R.array.tiendas);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.tiendas, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTienda.setAdapter(adapter);

        //ASIGNACIÓN DE HORAS
        setUpHoraSpinner(view);

        //MÉTODO DEL BOTÓN
        view.findViewById(R.id.solicitarB4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String descripcion = descripcionI.getText().toString();
                String hora = spinnerHora.getSelectedItem().toString();
                String tienda = spinnerTienda.getSelectedItem().toString();

                database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");
                nDatabase = database.getReference("cliente");
                Query query = nDatabase.orderByChild("email").equalTo(email);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("¿Dessea reservar la cita?")
                        .setMessage("El " + dia.toString() + " de " + mes.toString() + " a las " + hora.toString() + " en " + tienda.toString())
                        .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // Comprueba que se haya encontrado un cliente con el email dado
                                        if (dataSnapshot.exists()) {
                                            // Obtiene la referencia al cliente encontrado
                                            DataSnapshot clienteSnapshot = dataSnapshot.getChildren().iterator().next();
                                            DatabaseReference clienteRef = clienteSnapshot.getRef();

                                            // Añade una colección "citas" si no existe y añade una nueva cita
                                            DatabaseReference citasRef = clienteRef.child("citas").push();
                                            String citaId = citasRef.getKey();

                                            // Crear y asignar la nueva cita al cliente
                                            Cita cita = new Cita(mes, dia, hora, descripcion, tienda);
                                            citasRef.setValue(cita);

                                            // Actualizar la hora seleccionada a false
                                            DatabaseReference citasPorDiaRef = database.getReference("citas").child(mes).child(dia);
                                            citasPorDiaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.hasChild(hora)) {
                                                        DatabaseReference horaRef = citasPorDiaRef.child(hora);
                                                        horaRef.setValue(false);
                                                        Log.d("CITAS", "Hora " + hora + " actualizada a false en citas/" + mes + "/" + dia);
                                                    } else {
                                                        Log.d("CITAS", "No se encontró la hora " + hora + " en citas/" + mes + "/" + dia);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.w("CITAS", "Error al buscar la hora " + hora + " en citas/" + mes + "/" + dia, error.toException());
                                                }
                                            });



                                            descripcionI.setText("");
                                            fechaI.setText("");

                                            Toast.makeText(getContext(), "Se ha reservado la cita correctamente", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getContext(), "No se ha podido reservar la cita, por favor vuelve a intentarlo", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w("CITAS", "Error en la consulta", databaseError.toException());
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();

            }
        });

        return view;
    }

    private void setUpHoraSpinner(View view) {
        spinnerHora = view.findViewById(R.id.horaList);
        ArrayList<String> horaList = new ArrayList<>();
        ArrayAdapter horaAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, horaList);
        horaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHora.setAdapter(horaAdapter);

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
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, horas);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerHora.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Manejar error
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se necesita implementar
            }
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