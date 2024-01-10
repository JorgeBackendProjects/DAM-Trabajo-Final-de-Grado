package com.example.a14kapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a14kapp.Entidades.Cliente;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registro extends AppCompatActivity {

    private EditText nombreI, telefonoI, ciudadI, emailI, passwordI;
    private String nombre, telefono, ciudad, email, password;
    private CheckBox mostrarPassword;
    private Button registro;
    private Cliente cliente;
    private FirebaseDatabase database;
    private DatabaseReference nDatabase;
    private FirebaseAuth nAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");
        nDatabase = database.getReference("cliente");
        nAuth = FirebaseAuth.getInstance();

        nombreI = findViewById(R.id.nombreIR);
        telefonoI = findViewById(R.id.telefonoIR);
        ciudadI = findViewById(R.id.ciudadIR);
        emailI = findViewById(R.id.emailIR);
        passwordI = findViewById(R.id.passwordIR1);

        //rellenarCitas();

        mostrarPassword = findViewById(R.id.showPassword2);
        mostrarPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Mostrar la contraseña
                    passwordI.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    // Ocultar la contraseña
                    passwordI.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        registro = (Button) findViewById(R.id.buttonRegister);
        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nombre = nombreI.getText().toString();
                telefono = telefonoI.getText().toString();
                ciudad = ciudadI.getText().toString();
                email = emailI.getText().toString();
                password = passwordI.getText().toString();

                cliente = new Cliente(nombre, ciudad, telefono, email, password);

                nAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Registro.this, "CUENTA CREADA CON ÉXITO", Toast.LENGTH_LONG).show();
                                    //FirebaseUser cliente = nAuth.getCurrentUser();
                                    updateUI(cliente);
                                } else {
                                    Toast.makeText(Registro.this, "YA EXISTE UNA CUNETA CON ESTE EMAIL", Toast.LENGTH_LONG).show();
                                    updateUI(null);
                                }
                            }
                        });


            }
        });
    }

    public void updateUI(Cliente cliente){
        String keyID = nDatabase.push().getKey();
        nDatabase.child(keyID).setValue(cliente).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Intent i = new Intent(Registro.this, MainActivity.class);
                startActivity(i);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Registro.this, "Error al insertar el cliente en la base de datos", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /*public void rellenarCitas(){
        //List<String> dias = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31");
        //List<String> horario = Arrays.asList("10:00", "11:00", "12:00", "13:00", "17:00", "18:00", "19:00", "20:00");
        Map<String, Boolean> horario = new HashMap<>();
        horario.put("10:00", true);
        horario.put("11:00", true);
        horario.put("12:00", true);
        horario.put("13:00", true);
        horario.put("17:00", true);
        horario.put("18:00", true);
        horario.put("19:00", true);
        horario.put("20:00", true);

        Map<String, Map> dias = new HashMap<>();

        dias.put("1", horario);
        dias.put("2", horario);
        dias.put("3", horario);
        dias.put("4", horario);
        dias.put("5", horario);
        dias.put("6", horario);
        dias.put("7", horario);
        dias.put("8", horario);
        dias.put("9", horario);
        dias.put("10", horario);
        dias.put("11", horario);
        dias.put("12", horario);
        dias.put("13", horario);
        dias.put("14", horario);
        dias.put("15", horario);
        dias.put("16", horario);
        dias.put("17", horario);
        dias.put("18", horario);
        dias.put("19", horario);
        dias.put("20", horario);
        dias.put("21", horario);
        dias.put("22", horario);
        dias.put("23", horario);
        dias.put("24", horario);
        dias.put("25", horario);
        dias.put("26", horario);
        dias.put("27", horario);
        dias.put("28", horario);
        dias.put("29", horario);
        dias.put("30", horario);
        dias.put("31", horario);

        Map<String, Map> citas = new HashMap<>();

        citas.put("Enero", dias);
        citas.put("Febrero", dias);
        citas.put("Marzo", dias);
        citas.put("Abril", dias);
        citas.put("Mayo", dias);
        citas.put("Junio", dias);
        citas.put("Julio", dias);
        citas.put("Agosto", dias);
        citas.put("Septiembre", dias);
        citas.put("Octubre", dias);
        citas.put("Noviembre", dias);
        citas.put("Diciembre", dias);

        nDatabase.setValue(citas);
    }*/

}