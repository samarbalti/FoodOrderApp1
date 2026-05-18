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
    //Affiche toutes les commandes//
    private TextView tvCountPending, tvCountOnRoute, tvCountDelivered, tvCountCancelled;
    //Nombre de commandes "en cours", "en route", commandes delivrés et annullés//
    private OrdersAdapter adapter;
    //Adaptateur pour les commandes en mode administrateur (vrai)//
    private List<Order> orderList;
    private FirebaseFirestore db;
    private ImageView btnBack;
    //back button//

    @Override
    //Initializes all UI components including the four status counters//
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
        //Configure RecyclerView avec OrdersAdapter en mode administration//
        rvManageOrders.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        //Le bouton Retour termine l'activité//

        loadOrders();
    }

    private void loadOrders() {
        db.collection("orders")
            //Les commandes sont triées par horodatage en ordre décroissant (de la plus récente à la plus ancienne)//
            .orderBy("timestamp", Query.Direction.DESCENDING)
            //Utilise addSnapshotListener (écouteur en temps réel) au lieu d'une récupération unique//
            //L'écouteur d'instantané permet aux comptes de se mettre à jour automatiquement lorsque des commandes sont modifiées dans Firestore//
            .addSnapshotListener((value, error) -> {
                if (value != null) {
                    orderList.clear();
                    int pending = 0, onRoute = 0, delivered = 0, cancelled = 0;
                    for (QueryDocumentSnapshot doc : value) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        orderList.add(order);

                        //Pour chaque document, convertit en objet Commande et compte les statuts:"en cours", "en route", "livré", "annulé"//
                        String status = order.getStatus();
                        if ("En cours".equalsIgnoreCase(status)) pending++;
                        else if ("En route".equalsIgnoreCase(status)) onRoute++;
                        else if ("Livrée".equalsIgnoreCase(status) || "Livré".equalsIgnoreCase(status)) delivered++;
                        else if ("Annulée".equalsIgnoreCase(status) || "Annulé".equalsIgnoreCase(status)) cancelled++;
                    }
                    adapter.notifyDataSetChanged();
                    //Met à jour dynamiquement l'adaptateur et les quatre TextViews de compteur.//
                    
                    tvCountPending.setText("(" + pending + ")");
                    tvCountOnRoute.setText("(" + onRoute + ")");
                    tvCountDelivered.setText("(" + delivered + ")");
                    tvCountCancelled.setText("(" + cancelled + ")");
                }
            });
    }
}
