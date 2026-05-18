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
    //Affiche tous les produits alimentaires en mode administrateur//
    private FoodAdapter adapter;
    //Adaptateur RecyclerView avec rappel de suppression activé//
    private List<Food> foodList;
    //cache locale de produits alimentaires de Firestore//
    private FirebaseFirestore db;
    //instance de base de données Firestore//
    private ImageView btnBack, btnAdd;
    //Boutons de navigation arrière et de lancement de AddFoodActivity//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_foods);
        //gonfler activity_manage_foods.xml//

        db = FirebaseFirestore.getInstance();
        rvManageFoods = findViewById(R.id.rvManageFoods);
        btnBack = findViewById(R.id.btnBackManage);
        btnAdd = findViewById(R.id.btnAddFoodTop);

        rvManageFoods.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();
        adapter = new FoodAdapter(foodList, null, true, this::deleteFood);
        //Configure le RecyclerView avec FoodAdapter en mode administration (paramètre true), en passant this::deleteFood comme fonction de rappel de suppression//
        rvManageFoods.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        //appelle la fonction finish() pour revenir à l'écran précédent//
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddFoodActivity.class)));
        //lance AddFoodActivity via une intention//

        loadFoods();
    }

    private void loadFoods() {
        //Récupère tous les documents de la collection alimentaire//
        db.collection("foods").get().addOnSuccessListener(querySnapshot -> {
            foodList.clear();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                Food food = doc.toObject(Food.class);
                food.setId(doc.getId());
                foodList.add(food);
            }
            //Efface la liste des aliments, convertit chaque document Firestore en un objet Food à l'aide de la méthode toObject(), et conserve l'identifiant du document Firestore via la méthode setId()//
            adapter.notifyDataSetChanged();
            //Notifie l'adaptateur de rafraîchir l'interface utilisateur//
        });
    }
    //Supprime le document alimentaire spécifié de Firestore à l'aide de son ID//
    private void deleteFood(Food food) {
        db.collection("foods").document(food.getId()).delete()
            //En cas de succès : affiche un Toast et actualise la liste en appelant loadFoods()//
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Plat supprimé", Toast.LENGTH_SHORT).show();
                loadFoods();
            });
    }
}
