package com.example.a14kapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.rxjava3.annotations.NonNull;

public class ModificarDatos extends AppCompatActivity {

    private Bundle bundle;
    private static String email;

    private EditText nombreI, ciudadI, emailI, telefonoI;
    private Button guardarB;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_datos);

        bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getString("Email");
        }

        nombreI = findViewById(R.id.cambiar_nombre_I);
        ciudadI = findViewById(R.id.cambiar_ciudad_I);
        telefonoI = findViewById(R.id.cambiar_telefono_I);
        guardarB = findViewById(R.id.guardarCambiosB);

        cargarDatos();

        guardarB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Confirmar Modificación");
                builder.setMessage("¿Está seguro de que desea modificar los datos?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        guardarCambios();
                        ModificarDatos.super.onBackPressed();
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


    }

    public void cargarDatos(){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference clienteRef = database.getReference("cliente");

        Query query = clienteRef.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot clienteSnapshot = snapshot.getChildren().iterator().next();

                    // Obtener los valores actuales del cliente
                    String nombreActual = clienteSnapshot.child("nombre").getValue(String.class);
                    String ciudadActual = clienteSnapshot.child("ciudad").getValue(String.class);
                    String telefonoActual = clienteSnapshot.child("telefono").getValue(String.class);

                    nombreI.setText(nombreActual);
                    ciudadI.setText(ciudadActual);
                    telefonoI.setText(telefonoActual);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    public void guardarCambios(){
        String nuevoNombre = nombreI.getText().toString();
        String nuevaCiudad = ciudadI.getText().toString();
        String nuevoTelefono = telefonoI.getText().toString();

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference clienteRef = database.getReference("cliente");

        Query query = clienteRef.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot clienteSnapshot = snapshot.getChildren().iterator().next();
                    String clientId = clienteSnapshot.getKey();

                    clienteRef.child(clientId).child("nombre").setValue(nuevoNombre);
                    clienteRef.child(clientId).child("ciudad").setValue(nuevaCiudad);
                    clienteRef.child(clientId).child("telefono").setValue(nuevoTelefono)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ModificarDatos.this, "Datos modificados con éxito", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ModificarDatos.this, "Error al modificar los datos", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}