package com.example.foodorderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.MainActivity;
import com.example.foodorderapp.R;
import com.example.foodorderapp.adapters.OrdersAdapter;
import com.example.foodorderapp.models.Order;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private boolean isAdmin;
    private TextView filterAll, filterPending, filterDelivered, filterCancelled;
    private String currentFilter = "Toutes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        db = FirebaseFirestore.getInstance();
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        
        initViews();
        setupFilters();
        setupBottomNavigation();
        loadOrders();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        adapter = new OrdersAdapter(orderList, isAdmin);
        recyclerView.setAdapter(adapter);

        filterAll = findViewById(R.id.filterAll);
        filterPending = findViewById(R.id.filterPending);
        filterDelivered = findViewById(R.id.filterDelivered);
        filterCancelled = findViewById(R.id.filterCancelled);
    }

    private void setupFilters() {
        View.OnClickListener filterListener = v -> {
            // Reset colors
            filterAll.setBackgroundResource(R.drawable.filter_unselected_bg);
            filterPending.setBackgroundResource(R.drawable.filter_unselected_bg);
            filterDelivered.setBackgroundResource(R.drawable.filter_unselected_bg);
            filterCancelled.setBackgroundResource(R.drawable.filter_unselected_bg);
            filterAll.setTextColor(getResources().getColor(R.color.black));
            filterPending.setTextColor(getResources().getColor(R.color.black));
            filterDelivered.setTextColor(getResources().getColor(R.color.black));
            filterCancelled.setTextColor(getResources().getColor(R.color.black));

            // Select clicked
            v.setBackgroundResource(R.drawable.filter_selected_bg);
            ((TextView)v).setTextColor(getResources().getColor(R.color.white));

            if (v.getId() == R.id.filterAll) currentFilter = "Toutes";
            else if (v.getId() == R.id.filterPending) currentFilter = "En cours";
            else if (v.getId() == R.id.filterDelivered) currentFilter = "Livrée";
            else if (v.getId() == R.id.filterCancelled) currentFilter = "Annulée";

            loadOrders();
        };

        filterAll.setOnClickListener(filterListener);
        filterPending.setOnClickListener(filterListener);
        filterDelivered.setOnClickListener(filterListener);
        filterCancelled.setOnClickListener(filterListener);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_orders);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
                finish();
                return true;
            }
            return id == R.id.nav_orders;
        });
    }

    private void loadOrders() {
        Query query = db.collection("orders");
        
        if (!isAdmin) {
            String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            query = query.whereEqualTo("userId", userId);
        } else {
            // Si admin, on veut voir toutes les commandes, mais Firestore nécessite parfois 
            // un index composite si on mélange orderBy et where sur des champs différents.
        }

        if (currentFilter != null && !currentFilter.equals("Toutes")) {
            query = query.whereEqualTo("status", currentFilter);
        }

        query.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    String msg = error.getMessage();
                    if (msg != null && msg.contains("index")) {
                        Toast.makeText(this, "Index Firestore manquant. Vérifiez la console.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Erreur : " + msg, Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                if (value != null) {
                    orderList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        orderList.add(order);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
    }
}
