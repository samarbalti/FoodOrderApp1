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
    //Affiche la liste des produits alimentaires actuellement dans le panier//
    private TextView tvTotal;
    //Affiche le prix total de tous les articles du panier //
    private Button btnOrder;
    //Déclenche le processus de passation de commande //
    private List<Food> cartList;
    // Contient les aliments transmis depuis l'écran précédent //
    private double total = 0;
    // Cumule la somme des prix de tous les articles //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Point d'entrée au début de l'activité //
        setContentView(R.layout.activity_cart);
        // pour gonfler la mise en page XML //

        cartList = getIntent().getParcelableArrayListExtra("cart");
        // Récupère les articles du panier à partir de l'intention//
        if (cartList == null) cartList = new ArrayList<>();
        
        // Initialise les composants d'interface utilisateur //
        recyclerView = findViewById(R.id.recyclerViewCart);
        tvTotal = findViewById(R.id.tvTotal);
        btnOrder = findViewById(R.id.btnOrder);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Établit une liste verticale//
        FoodAdapter adapter = new FoodAdapter(cartList, null, false, null);
        //Crée un FoodAdapter avec null click listener et la valeur false pour admin mode (read-only cart view) //
        recyclerView.setAdapter(adapter);

        for (Food f : cartList) total += f.getPrice();
        //Calcule le prix total en parcourant la liste du panier//
        tvTotal.setText(String.format("Total: %.2f €", total));
        //Formate et affiche le total comme « Total : XX,XX € »//

        btnOrder.setOnClickListener(v -> placeOrder());
        //Associe la fonction placeOrder() à l'écouteur de clic du bouton de commande//
    }

    private void placeOrder() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //Récupère l'UID Firebase de l'utilisateur actuellement connecté//

        // Récupérez d'abord les informations de l'utilisateur à inclure dans la commande//
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
                //Crée un nouvel objet Commande//

                //Enregistre la commande dans la collection de commandes de Firestore.//
                FirebaseFirestore.getInstance()
                    .collection("orders")
                    .document(orderId)
                    .set(order)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Commande passée !", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    //En cas de succès : affiche une confirmation Toast et ferme l’activité (finish()).//
                    //En cas d'échec : affiche le message d'erreur//
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erreur lors de la récupération des infos utilisateur", Toast.LENGTH_SHORT).show();
            });
    }
}
