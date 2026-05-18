package com.example.foodorderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.R;
import com.example.foodorderapp.adapters.FoodAdapter;
import com.example.foodorderapp.models.Food;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageFoodsActivity extends AppCompatActivity {

    private RecyclerView rvManageFoods;
    private FoodAdapter adapter;
    private List<Food> foodList;
    private FirebaseFirestore db;
    private ImageView btnBack, btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_foods);

        db = FirebaseFirestore.getInstance();
        rvManageFoods = findViewById(R.id.rvManageFoods);
        btnBack = findViewById(R.id.btnBackManage);
        btnAdd = findViewById(R.id.btnAddFoodTop);

        rvManageFoods.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();
        adapter = new FoodAdapter(foodList, null, true, this::deleteFood);
        rvManageFoods.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddFoodActivity.class)));

        loadFoods();
    }

    private void loadFoods() {
        db.collection("foods").get().addOnSuccessListener(querySnapshot -> {
            foodList.clear();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                Food food = doc.toObject(Food.class);
                food.setId(doc.getId());
                foodList.add(food);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void deleteFood(Food food) {
        db.collection("foods").document(food.getId()).delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Plat supprimé", Toast.LENGTH_SHORT).show();
                loadFoods();
            });
    }
}
