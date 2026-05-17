package com.example.foodorderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorderapp.R;
import com.example.foodorderapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etPhone, etAddress;
    private Button btnRegister;
    private TextView btnGoToLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etAddress = findViewById(R.id.etAddress);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        btnRegister.setOnClickListener(v -> registerUser());
        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mot de passe minimum 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Interdire inscription avec email admin
        if (email.equalsIgnoreCase("admin@foodorder.com")) {
            Toast.makeText(this, "Ce compte existe deja ! Connectez-vous.", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userId = mAuth.getCurrentUser().getUid();
                    User user = new User(userId, name, email, "client", phone, address);

                    db.collection("users").document(userId).set(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Inscription reussie !", Toast.LENGTH_SHORT).show();
                            // Deconnexion pour forcer le login
                            mAuth.signOut();
                            // Aller a LoginActivity
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue";
                    if (error.contains("already in use")) {
                        Toast.makeText(this, "Cet email existe deja !", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Erreur : " + error, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
}
