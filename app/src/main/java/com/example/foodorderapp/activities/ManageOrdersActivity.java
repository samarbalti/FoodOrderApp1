package com.example.foodorderapp.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.R;
import com.example.foodorderapp.adapters.OrdersAdapter;
import com.example.foodorderapp.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity {

    private RecyclerView rvManageOrders;
    private TextView tvCountPending, tvCountOnRoute, tvCountDelivered, tvCountCancelled;
    private OrdersAdapter adapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        db = FirebaseFirestore.getInstance();
        rvManageOrders = findViewById(R.id.rvManageOrders);
        btnBack = findViewById(R.id.btnBackOrders);
        
        tvCountPending = findViewById(R.id.tvCountManagePending);
        tvCountOnRoute = findViewById(R.id.tvCountManageOnRoute);
        tvCountDelivered = findViewById(R.id.tvCountManageDelivered);
        tvCountCancelled = findViewById(R.id.tvCountManageCancelled);

        rvManageOrders.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        adapter = new OrdersAdapter(orderList, true);
        rvManageOrders.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadOrders();
    }

    private void loadOrders() {
        db.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (value != null) {
                    orderList.clear();
                    int pending = 0, onRoute = 0, delivered = 0, cancelled = 0;
                    for (QueryDocumentSnapshot doc : value) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        orderList.add(order);
                        
                        String status = order.getStatus();
                        if ("En cours".equalsIgnoreCase(status)) pending++;
                        else if ("En route".equalsIgnoreCase(status)) onRoute++;
                        else if ("Livrée".equalsIgnoreCase(status) || "Livré".equalsIgnoreCase(status)) delivered++;
                        else if ("Annulée".equalsIgnoreCase(status) || "Annulé".equalsIgnoreCase(status)) cancelled++;
                    }
                    adapter.notifyDataSetChanged();
                    
                    tvCountPending.setText("(" + pending + ")");
                    tvCountOnRoute.setText("(" + onRoute + ")");
                    tvCountDelivered.setText("(" + delivered + ")");
                    tvCountCancelled.setText("(" + cancelled + ")");
                }
            });
    }
}
