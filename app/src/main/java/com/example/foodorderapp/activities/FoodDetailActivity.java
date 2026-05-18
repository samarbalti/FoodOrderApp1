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
        //gonfler activity_food_detail.xml//

        Food food = getIntent().getParcelableExtra("food");
        //Récupère l'objet Nourriture à partir de l'intention//
        //Si aucun aliment n'est passé, l'activité se termine immédiatement.//
        if (food == null) {
            finish();
            return;
        }

        ImageView imgFood = findViewById(R.id.imgFoodDetail);
        //	Affiche l'image du plat à l'aide de la bibliothèque de chargement d'images Glide.//
        TextView tvName = findViewById(R.id.tvFoodNameDetail);
        //Affiche le nom de l'aliment//
        TextView tvPrice = findViewById(R.id.tvFoodPriceDetail);
        //Affiche le prix au format « XX,XX € »//
        TextView tvDesc = findViewById(R.id.tvFoodDescDetail);
        //Affiche la description de l'aliment//
        ExtendedFloatingActionButton btnAddToCart = findViewById(R.id.btnAddToCartDetail);
        //Bouton d'action Material Design pour ajouter un article au panier//

        tvName.setText(food.getName());
        tvPrice.setText(String.format("%.2f €", food.getPrice()));
        tvDesc.setText(food.getDescription());
        //Définit les valeurs textuelles de l'objet Nourriture//
        
        //Utilise Glide pour charger l'URL de l'image de manière asynchrone avec un recadrage centré.//
        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(food.getImageUrl())
                .centerCrop()
                .into(imgFood);
        }

        btnAddToCart.setOnClickListener(v -> {
            // Dans une application réelle, nous ajouterions les articles à un panier ou une base de données globale//
            Toast.makeText(this, food.getName() + " ajouté au panier", Toast.LENGTH_SHORT).show();
            // Nous pourrions également terminer et renvoyer le résultat à MainActivity//
        });
    }
}
