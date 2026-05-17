package com.example.foodorderapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.R;
import com.example.foodorderapp.adapters.FoodAdapter;
import com.example.foodorderapp.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvClientName, tvClientPhone, tvAddress, tvTotal, tvDetailStatus;
    private RecyclerView rvItems;
    private Spinner spinnerStatus;
    private Button btnSave, btnCancel;
    private ImageView btnBack;
    private Order order;
    private FirebaseFirestore db;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        db = FirebaseFirestore.getInstance();
        order = (Order) getIntent().getSerializableExtra("order");
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvClientName = findViewById(R.id.tvDetailClientName);
        tvClientPhone = findViewById(R.id.tvDetailClientPhone);
        tvAddress = findViewById(R.id.tvDetailAddress);
        tvTotal = findViewById(R.id.tvDetailTotal);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        rvItems = findViewById(R.id.rvDetailItems);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSave = findViewById(R.id.btnSaveStatus);
        btnCancel = findViewById(R.id.btnCancelOrderDetail);
        btnBack = findViewById(R.id.btnBackDetail);

        if (order != null) {
            displayOrderDetails();
        }

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveStatus());
        btnCancel.setOnClickListener(v -> cancelOrder());
    }

    private void displayOrderDetails() {
        tvTitle.setText("Détail commande #" + (order.getId() != null && order.getId().length() > 5 ? order.getId().substring(0, 5).toUpperCase() : "ID"));
        
        // Afficher les infos client si elles existent dans l'objet Order
        if (order.getUserName() != null && !order.getUserName().isEmpty()) {
            tvClientName.setText(order.getUserName());
            tvClientPhone.setText("Num: " + order.getUserPhone());
            tvAddress.setText(order.getUserAddress());
        } else {
            // Sinon, essayer de les recuperer depuis Firestore via userId
            tvClientName.setText("Chargement...");
            db.collection("users").document(order.getUserId()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String phone = document.getString("phone");
                        String address = document.getString("address");
                        tvClientName.setText(name != null ? name : "Inconnu");
                        tvClientPhone.setText("Num: " + (phone != null ? phone : "N/A"));
                        tvAddress.setText(address != null ? address : "N/A");
                        
                        // Mettre a jour l'objet local
                        order.setUserName(name);
                        order.setUserPhone(phone);
                        order.setUserAddress(address);
                    } else {
                        tvClientName.setText("Client inconnu");
                    }
                })
                .addOnFailureListener(e -> {
                    tvClientName.setText("Erreur chargement");
                });
        }
        
        tvTotal.setText(String.format("%.2f DT", order.getTotalPrice()));
        tvDetailStatus.setText(String.valueOf(order.getStatus()));

        // Use FoodAdapter for items (read-only mode)
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        FoodAdapter itemsAdapter = new FoodAdapter(order.getItems(), null, false, null);
        rvItems.setAdapter(itemsAdapter);

        // Setup Spinner
        String[] statuses = {"En cours", "En route", "Livrée", "Annulée"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        if (isAdmin) {
            spinnerStatus.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            tvDetailStatus.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
        } else {
            spinnerStatus.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
            tvDetailStatus.setVisibility(View.VISIBLE);
            
            // Show cancel only if pending
            if (order.getStatus() != null && "En cours".equalsIgnoreCase(order.getStatus().toString())) {
                btnCancel.setVisibility(View.VISIBLE);
            } else {
                btnCancel.setVisibility(View.GONE);
            }
        }

        // Select current status
        if (order.getStatus() != null) {
            String status = order.getStatus().toString();
            for (int i = 0; i < statuses.length; i++) {
                if (statuses[i].equalsIgnoreCase(status)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveStatus() {
        String newStatus = spinnerStatus.getSelectedItem().toString();
        db.collection("orders").document(order.getId())
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Statut mis à jour", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void cancelOrder() {
        db.collection("orders").document(order.getId())
            .update("status", "Annulée")
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Commande annulée", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
