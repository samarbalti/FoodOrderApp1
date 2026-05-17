package com.example.foodorderapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.activities.AddFoodActivity;
import com.example.foodorderapp.activities.CartActivity;
import com.example.foodorderapp.activities.LoginActivity;
import com.example.foodorderapp.activities.OrdersActivity;
import com.example.foodorderapp.adapters.FoodAdapter;
import com.example.foodorderapp.models.Food;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.foodorderapp.activities.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private List<Food> foodList;
    private List<Food> cartList = new ArrayList<>();
    private SearchView searchView;
    private FloatingActionButton fabAddFood;
    private ImageView btnNotifications;
    private BottomNavigationView bottomNavigation;
    private TextView tvUserRole;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        fabAddFood = findViewById(R.id.fabAddFood);
        btnNotifications = findViewById(R.id.btnNotifications);
        tvUserRole = findViewById(R.id.tvUserRole);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();

        updateUI();
        setupBottomNavigation();
        loadFoods();

        btnNotifications.setOnClickListener(v -> {
            Toast.makeText(this, "Pas de nouvelles notifications", Toast.LENGTH_SHORT).show();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFoods(newText);
                return true;
            }
        });

        FloatingActionButton fabCart = findViewById(R.id.fabCart);

        fabCart.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(this, "Panier vide", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CartActivity.class);
            intent.putParcelableArrayListExtra("cart", new ArrayList<>(cartList));
            startActivity(intent);
        });

        fabAddFood.setOnClickListener(v -> {
            startActivity(new Intent(this, AddFoodActivity.class));
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_orders) {
                Intent intent = new Intent(this, OrdersActivity.class);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void updateUI() {
        if (isAdmin) {
            tvUserRole.setText("Administrateur");
            adapter = new FoodAdapter(foodList, null, true, this::deleteFood);
            fabAddFood.setVisibility(View.VISIBLE);
        } else {
            tvUserRole.setText("Client");
            adapter = new FoodAdapter(foodList, this::addToCart, false, null);
            fabAddFood.setVisibility(View.GONE);
        }
        recyclerView.setAdapter(adapter);
    }

    private void loadFoods() {
        db.collection("foods")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                foodList.clear();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Food food = doc.toObject(Food.class);
                    food.setId(doc.getId());
                    foodList.add(food);
                }
                adapter.notifyDataSetChanged();
            });
    }

    private void addToCart(Food food) {
        cartList.add(food);
        Toast.makeText(this, food.getName() + " ajouté au panier", Toast.LENGTH_SHORT).show();
    }

    private void deleteFood(Food food) {
        db.collection("foods").document(food.getId()).delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, food.getName() + " supprimé", Toast.LENGTH_SHORT).show();
                loadFoods();
            });
    }

    private void filterFoods(String query) {
        List<Food> filtered = new ArrayList<>();
        for (Food food : foodList) {
            if (food.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(food);
            }
        }
        adapter = new FoodAdapter(filtered, 
            isAdmin ? null : this::addToCart, 
            isAdmin, 
            isAdmin ? this::deleteFood : null);
        recyclerView.setAdapter(adapter);
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFoods();
    }
}
