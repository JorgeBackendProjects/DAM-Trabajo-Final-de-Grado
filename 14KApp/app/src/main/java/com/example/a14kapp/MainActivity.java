package com.example.a14kapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText emailI, passwordI;
    private TextView registro;
    private Button botonLogin;
    private CheckBox mostrarPassword;

    private FirebaseDatabase database;
    private FirebaseAuth nAuth;
    private AuthCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");
        nAuth = FirebaseAuth.getInstance();

        emailI = findViewById(R.id.emailI);
        passwordI = findViewById(R.id.passwordI);
        mostrarPassword = findViewById(R.id.showPassword);
        registro = findViewById(R.id.registroL);

        resaltarTexto();

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

        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Registro.class);
                startActivity(i);
            }
        });

        botonLogin = (Button) findViewById(R.id.loginB);
        botonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = emailI.getText().toString();
                String userPassword = passwordI.getText().toString();

                if (!userEmail.isEmpty() && !userPassword.isEmpty()) {
                    credential = EmailAuthProvider.getCredential(userEmail, userPassword);

                    nAuth.signInWithCredential(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = nAuth.getCurrentUser();

                                    Intent i = new Intent(MainActivity.this, Inicio.class);
                                    i.putExtra("email", userEmail);

                                    emailI.setText("");
                                    passwordI.setText("");

                                    startActivity(i);
                                } else if (userEmail.isEmpty() || userPassword.isEmpty()) {
                                    Toast.makeText(MainActivity.this, "Email / contraseña incorrectos", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Email / contraseña incorrectos", Toast.LENGTH_LONG).show();
                                }
                            });
                }else{
                    Toast.makeText(MainActivity.this, "Email / contraseña incorrectos", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void resaltarTexto(){
        String registroTitulo = registro.getText().toString();
        String resaltar = "Regístrate aquí";

        SpannableString spannable = new SpannableString(registroTitulo);

        int startIndex = registroTitulo.indexOf(resaltar);
        int endIndex = startIndex + resaltar.length();

        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.BLUE);
        spannable.setSpan(foregroundColorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        registro.setText(spannable);
    }

}