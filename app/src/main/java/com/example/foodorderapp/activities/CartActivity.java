package com.example.foodorderapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.R;
import com.example.foodorderapp.adapters.FoodAdapter;
import com.example.foodorderapp.models.Food;
import com.example.foodorderapp.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView tvTotal;
    private Button btnOrder;
    private List<Food> cartList;
    private double total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartList = getIntent().getParcelableArrayListExtra("cart");
        if (cartList == null) cartList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerViewCart);
        tvTotal = findViewById(R.id.tvTotal);
        btnOrder = findViewById(R.id.btnOrder);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FoodAdapter adapter = new FoodAdapter(cartList, null, false, null);
        recyclerView.setAdapter(adapter);

        for (Food f : cartList) total += f.getPrice();
        tvTotal.setText(String.format("Total: %.2f €", total));

        btnOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Fetch user info first to include in the order
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener(document -> {
                String userName = "Client";
                String userPhone = "N/A";
                String userAddress = "N/A";
                
                if (document.exists()) {
                    userName = document.getString("name");
                    userPhone = document.getString("phone");
                    userAddress = document.getString("address");
                }
                
                String orderId = FirebaseFirestore.getInstance().collection("orders").document().getId();

                Order order = new Order();
                order.setId(orderId);
                order.setUserId(userId);
                order.setUserName(userName);
                order.setUserPhone(userPhone);
                order.setUserAddress(userAddress);
                order.setItems(cartList);
                order.setTotalPrice(total);
                order.setStatus("En cours");
                order.setTimestamp(System.currentTimeMillis());

                FirebaseFirestore.getInstance()
                    .collection("orders")
                    .document(orderId)
                    .set(order)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Commande passée !", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erreur lors de la récupération des infos utilisateur", Toast.LENGTH_SHORT).show();
            });
    }
}
