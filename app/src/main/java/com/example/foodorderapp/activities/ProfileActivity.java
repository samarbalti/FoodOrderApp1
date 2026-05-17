package com.example.foodorderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorderapp.MainActivity;
import com.example.foodorderapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private LinearLayout btnMyOrders, btnLogoutProfile;
    private BottomNavigationView bottomNavigation;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        btnMyOrders = findViewById(R.id.btnMyOrders);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        loadUserData();

        btnMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrdersActivity.class);
            intent.putExtra("isAdmin", isAdmin);
            startActivity(intent);
        });

        btnLogoutProfile.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNavigation();
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            tvProfileEmail.setText(email);
            
            db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        tvProfileName.setText(document.getString("name"));
                    }
                });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_orders) {
                Intent intent = new Intent(this, OrdersActivity.class);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}
