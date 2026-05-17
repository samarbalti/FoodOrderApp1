package com.example.foodorderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorderapp.MainActivity;
import com.example.foodorderapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView btnGoToRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Si deja connecte, verifier le role et aller a MainActivity
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            checkUserRoleAndRedirect(userId);
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userId = mAuth.getCurrentUser().getUid();
                    checkUserRoleAndRedirect(userId);
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue";
                    Toast.makeText(this, "Erreur : " + error, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void checkUserRoleAndRedirect(String userId) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener(document -> {
                boolean isAdmin = false;
                if (document.exists()) {
                    String role = document.getString("role");
                    isAdmin = "admin".equals(role);
                }
                Intent intent;
                if (isAdmin) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
}
