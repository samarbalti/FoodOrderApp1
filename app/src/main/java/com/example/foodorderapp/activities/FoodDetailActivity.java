package com.example.foodorderapp.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.models.Food;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class FoodDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        Food food = getIntent().getParcelableExtra("food");
        if (food == null) {
            finish();
            return;
        }

        ImageView imgFood = findViewById(R.id.imgFoodDetail);
        TextView tvName = findViewById(R.id.tvFoodNameDetail);
        TextView tvPrice = findViewById(R.id.tvFoodPriceDetail);
        TextView tvDesc = findViewById(R.id.tvFoodDescDetail);
        ExtendedFloatingActionButton btnAddToCart = findViewById(R.id.btnAddToCartDetail);

        tvName.setText(food.getName());
        tvPrice.setText(String.format("%.2f €", food.getPrice()));
        tvDesc.setText(food.getDescription());

        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(food.getImageUrl())
                .centerCrop()
                .into(imgFood);
        }

        btnAddToCart.setOnClickListener(v -> {
            // In a real app, we would add to a global cart or database
            Toast.makeText(this, food.getName() + " ajouté au panier", Toast.LENGTH_SHORT).show();
            // We could also finish and return the result to MainActivity
        });
    }
}
