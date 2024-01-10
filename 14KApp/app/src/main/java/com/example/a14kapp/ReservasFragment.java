package com.example.a14kapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a14kapp.Entidades.Cita;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReservasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReservasFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static String email;
    private static View view;

    private FirebaseDatabase database;
    private DatabaseReference database2;
    private DatabaseReference nDatabase;

    private RecyclerView recyclerView;
    private static ReservasAdapter adapter;
    private List<Cita> citaList;

    public ReservasFragment(String email) {
        // Required empty public constructor
        this.email = email;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReservasFragment newInstance(String param1, String param2) {
        ReservasFragment fragment = new ReservasFragment(email);
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

        database2 = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("cliente");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reservas, container, false);

        recyclerView = view.findViewById(R.id.recyclerView02);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        citaList = new ArrayList<>();

        //CARGAR LAS RERSEVAS
        database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");
        nDatabase = database.getReference("cliente");
        Query query = nDatabase.orderByChild("email").equalTo(email);
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
                            citaList.clear();

                            for (DataSnapshot citaSnapshot : dataSnapshot.getChildren()) {
                                Cita cita = citaSnapshot.getValue(Cita.class);
                                citaList.add(cita);
                            }

                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        adapter = new ReservasFragment.ReservasAdapter(citaList, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }


    public static class ReservasAdapter extends RecyclerView.Adapter<ReservasAdapter.ReservasViewHolder> {

        private List<Cita> citaList;
        private Context context;

        public ReservasAdapter(List<Cita> citaList, Context context) {
            this.citaList = citaList;
            this.context = context;
        }

        @NonNull
        @Override
        public ReservasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reservas_item, parent, false);
            return new ReservasViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReservasViewHolder holder, int position) {
            Cita cita = citaList.get(position);

            holder.tiendaTextView.setText("Tienda: " + cita.getTienda());
            holder.descriptionTextView.setText("Descripción: " + cita.getDescripcion());
            holder.fechaTextView.setText("Fecha: " + cita.getDia() + " de " + cita.getMes());
            holder.horaTextView.setText("Hora: " + cita.getHora());

            holder.eliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Anular Reserva");
                    builder.setMessage("¿Está seguro de que desea anular la reserva?");
                    builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String mes = transformarMes(citaList.get(holder.getAdapterPosition()).getMes());
                            String dia = citaList.get(holder.getAdapterPosition()).getDia();
                            String hora = citaList.get(holder.getAdapterPosition()).getHora();

                            DatabaseReference clienteRef = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("cliente");
                            Query query = clienteRef.orderByChild("email").equalTo(email);

                            //VOLVER A PONER LA HORA A TRUE PARA QUE VUELVA A ESTAR DISPONIBLE
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        DataSnapshot clienteSnapshot = dataSnapshot.getChildren().iterator().next();
                                        DatabaseReference clienteRef = clienteSnapshot.getRef();
                                        DatabaseReference citasRef = clienteRef.child("citas").child(mes).child(dia).child(hora);

                                        citasRef.removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        DatabaseReference citasIndependienteRef = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("citas")
                                                                .child(mes).child(dia).child(hora);
                                                        citasIndependienteRef.setValue(true)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        adapter.notifyDataSetChanged();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {}
                                                                });

                                                        adapter.notifyDataSetChanged();
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

                            //ELIMINAR LA CITA DE LA SUBCOLECCIÓN DEL CLIENTE
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
                                                        String citaMes = citaSnapshot.child("mes").getValue(String.class);
                                                        String citaDia = citaSnapshot.child("dia").getValue(String.class);
                                                        String citaHora = citaSnapshot.child("hora").getValue(String.class);

                                                        if (citaMes.equals(mes) && citaDia.equals(dia) && citaHora.equals(hora)) {
                                                            String citaKey = citaSnapshot.getKey();

                                                            citasRef.child(citaKey).removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(view.getContext(), "La reserva se ha anulado correctamente", Toast.LENGTH_SHORT).show();
                                                                            adapter.notifyDataSetChanged();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(view.getContext(), "Error al anular la reserva", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

                                                            adapter.notifyDataSetChanged();
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

                            //VOLVER A CARGAR LOS DATOS
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        DataSnapshot clienteSnapshot = dataSnapshot.getChildren().iterator().next();
                                        DatabaseReference clienteRef = clienteSnapshot.getRef();
                                        DatabaseReference citasRef = clienteRef.child("citas");

                                        citasRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                citaList.clear();
                                                for (DataSnapshot citaSnapshot : dataSnapshot.getChildren()) {
                                                    Cita cita = citaSnapshot.getValue(Cita.class);
                                                    citaList.add(cita);
                                                }

                                                adapter.setCitaList(citaList);
                                                adapter.notifyDataSetChanged();
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
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Acciones a realizar cuando el usuario elige "No"
                            // Aquí puedes agregar cualquier acción adicional o simplemente cerrar el diálogo
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            holder.modificar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(view.getContext(), ModificarReserva.class);
                    intent.putExtra("Email", email);
                    intent.putExtra("Mes", cita.getMes());
                    intent.putExtra("Dia", cita.getDia());
                    intent.putExtra("Hora", cita.getHora());
                    intent.putExtra("Descripcion", cita.getDescripcion());
                    intent.putExtra("Tienda", cita.getTienda());
                    view.getContext().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return citaList.size();
        }

        public void setCitaList(List<Cita> citaList) {
            this.citaList = citaList;
            notifyDataSetChanged();
        }

        public static class ReservasViewHolder extends RecyclerView.ViewHolder {

            public TextView tiendaTextView, descriptionTextView, fechaTextView, horaTextView;
            public Button eliminar, modificar;

            public ReservasViewHolder(@NonNull View itemView) {
                super(itemView);
                tiendaTextView = itemView.findViewById(R.id.tienda_item_R);
                descriptionTextView = itemView.findViewById(R.id.descripcion_item_R);
                fechaTextView = itemView.findViewById(R.id.fecha_item_R);
                horaTextView = itemView.findViewById(R.id.hora_item_R);
                eliminar = itemView.findViewById(R.id.eliminarR_Button);
                modificar = itemView.findViewById(R.id.modificarR_Button);
            }
        }
    }

    private void reactivarCita(){

    }

    private static String transformarMes(String mes){
        switch(mes){
            case "01":
                mes = "Enero";
                break;
            case "1":
                mes = "Enero";
                break;
            case "02":
                mes = "Febrero";
                break;
            case "2":
                mes = "Febrero";
                break;
            case "03":
                mes = "Marzo";
                break;
            case "3":
                mes = "Marzo";
                break;
            case "04":
                mes = "Abril";
                break;
            case "4":
                mes = "Abril";
                break;
            case "05":
                mes = "Mayo";
                break;
            case "5":
                mes = "Mayo";
                break;
            case "06":
                mes = "Junio";
                break;
            case "6":
                mes = "Junio";
                break;
            case "07":
                mes = "Julio";
                break;
            case "7":
                mes = "Julio";
                break;
            case "08":
                mes = "Agosto";
                break;
            case "8":
                mes = "Agosto";
                break;
            case "09":
                mes = "Septiembre";
                break;
            case "9":
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

}




