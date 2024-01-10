package com.example.a14kapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.rxjava3.annotations.NonNull;

public class CambiarPassword extends AppCompatActivity {

    private Button submit;
    private static EditText oldPassword, newPassword;
    private CheckBox mostrar;
    private Bundle bundle;
    private static String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_password);

        bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getString("Email");
            Log.i("EMAIL", email);
        }

        oldPassword = findViewById(R.id.pC1);
        newPassword = findViewById(R.id.pC2);
        submit = findViewById(R.id.boton_cambiar_password2);
        mostrar = findViewById(R.id.mostrar_password_ckeckbox);

        mostrar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Mostrar la contraseña
                    oldPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    newPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    // Ocultar la contraseña
                    oldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    newPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarPasswordAtributo();
                cambiarPasswordOriginal();
            }
        });

    }

    private void cambiarPasswordAtributo(){
        String oldPasswordM = oldPassword.getText().toString();
        String newPasswordM = newPassword.getText().toString();

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference clienteRef = database.getReference("cliente");

        Query query = clienteRef.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Se encontró un cliente con el email especificado
                    DataSnapshot clienteSnapshot = snapshot.getChildren().iterator().next();
                    String clientId = clienteSnapshot.getKey();

                    // Obtener la contraseña actual del cliente
                    String clientePassword = clienteSnapshot.child("password").getValue(String.class);

                    Log.i("OLD", oldPasswordM.toString());

                    if (clientePassword.equals(oldPasswordM.toString())) {
                        // La contraseña coincide, realizar el reemplazo con la segunda contraseña
                        clienteRef.child(clientId).child("password").setValue(newPasswordM.toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(CambiarPassword.this, "Contraseña reemplazada con éxito", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CambiarPassword.this, "Error al reemplazar la contraseña", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // La contraseña no coincide
                        Toast.makeText(CambiarPassword.this, "La contraseña no es correcta", Toast.LENGTH_SHORT).show();
                    }
                } else {}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void cambiarPasswordOriginal() {
        String oldPasswordM = oldPassword.getText().toString();
        String newPasswordM = newPassword.getText().toString();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");

        // Autenticar al usuario con su email y contraseña antigua
        mAuth.signInWithEmailAndPassword(email, oldPasswordM)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                currentUser.updatePassword(newPasswordM)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    DatabaseReference clienteRef = mDatabase.getReference("cliente");
                                                    clienteRef.orderByChild("email").equalTo(email)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.exists()) {
                                                                        DataSnapshot clienteSnapshot = snapshot.getChildren().iterator().next();
                                                                        String clientId = clienteSnapshot.getKey();

                                                                        clienteRef.child(clientId).child("password").setValue(newPassword)
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            Toast.makeText(CambiarPassword.this, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show();
                                                                                        } else {
                                                                                            Toast.makeText(CambiarPassword.this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                    } else {}
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {}
                                                            });
                                                } else {
                                                    Toast.makeText(CambiarPassword.this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(CambiarPassword.this, "La contraseña actual no es correcta", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}