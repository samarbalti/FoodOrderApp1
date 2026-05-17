package com.example.foodorderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.MainActivity;
import com.example.foodorderapp.R;
import com.example.foodorderapp.adapters.OrdersAdapter;
import com.example.foodorderapp.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvCountPending, tvCountDelivered, tvCountOnRoute, tvCountCancelled;
    private ImageView btnAdminLogout, btnGoToFoods, btnGoToOrders;
    private android.widget.Button btnSuivreCommandes;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvCountPending = findViewById(R.id.tvCountPending);
        tvCountDelivered = findViewById(R.id.tvCountDelivered);
        tvCountOnRoute = findViewById(R.id.tvCountOnRoute);
        tvCountCancelled = findViewById(R.id.tvCountCancelled);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);
        btnGoToFoods = findViewById(R.id.btnGoToFoods);
        btnGoToOrders = findViewById(R.id.btnGoToOrders);
        btnSuivreCommandes = findViewById(R.id.btnSuivreCommandes);

        loadStats();

        // Animations
        ((android.view.View) findViewById(R.id.tvCountPending).getParent().getParent()).startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up));

        btnAdminLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnGoToFoods.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageFoodsActivity.class));
        });

        btnGoToOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageOrdersActivity.class));
        });

        btnSuivreCommandes.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageOrdersActivity.class);
            startActivity(intent);
        });
    }

    private void loadStats() {
        db.collection("orders").get()
            .addOnSuccessListener(querySnapshot -> {
                int pending = 0, delivered = 0, onRoute = 0, cancelled = 0;
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    String status = doc.getString("status");
                    if ("En cours".equalsIgnoreCase(status)) pending++;
                    else if ("En route".equalsIgnoreCase(status)) onRoute++;
                    else if ("Livrée".equalsIgnoreCase(status) || "Livré".equalsIgnoreCase(status)) delivered++;
                    else if ("Annulée".equalsIgnoreCase(status) || "Annulé".equalsIgnoreCase(status)) cancelled++;
                }

                tvCountPending.setText(String.valueOf(pending));
                tvCountDelivered.setText(String.valueOf(delivered));
                tvCountOnRoute.setText(String.valueOf(onRoute));
                tvCountCancelled.setText(String.valueOf(cancelled));
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erreur stats: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }
}
