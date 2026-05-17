package com.example.foodorderapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.R;
import com.example.foodorderapp.models.Food;
import com.example.foodorderapp.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private boolean isAdmin;

    public OrdersAdapter(List<Order> orderList, boolean isAdmin) {
        this.orderList = orderList;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateStr = "N/A";
        try {
            Object tsObj = order.getTimestamp();
            if (tsObj != null) {
                long ts;
                if (tsObj instanceof Long) {
                    ts = (Long) tsObj;
                } else if (tsObj instanceof String) {
                    ts = Long.parseLong((String) tsObj);
                } else if (tsObj instanceof com.google.firebase.Timestamp) {
                    ts = ((com.google.firebase.Timestamp) tsObj).toDate().getTime();
                } else {
                    ts = 0;
                }
                
                if (ts != 0) {
                    dateStr = sdf.format(new Date(ts));
                }
            }
        } catch (Exception e) {
            dateStr = String.valueOf(order.getTimestamp());
        }

        holder.tvOrderId.setText("Commande #" + (order.getId() != null && order.getId().length() > 5 ? order.getId().substring(0, 5).toUpperCase() : "ID"));
        holder.tvOrderDate.setText(dateStr);
        holder.tvOrderStatus.setText(order.getStatus());
        holder.tvOrderTotal.setText(String.format("Total: %.2f DT", order.getTotalPrice()));

        // Color status based on value
        int statusColor = android.graphics.Color.GRAY;
        String status = order.getStatus();
        if (status != null) {
            switch (status.toLowerCase()) {
                case "en cours": statusColor = 0xFFFF9800; break; // Orange
                case "en route": statusColor = 0xFF2196F3; break; // Blue
                case "livré": 
                case "livrée": statusColor = 0xFF4CAF50; break; // Green
                case "annulé": 
                case "annulée": statusColor = 0xFFF44336; break; // Red
            }
        }
        holder.tvOrderStatus.setTextColor(statusColor);

        StringBuilder foods = new StringBuilder();
        if (order.getItems() != null) {
            for (Food food : order.getItems()) {
                foods.append("• ").append(food.getName()).append("\n");
            }
        }
        holder.tvOrderFoods.setText(foods.toString());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), com.example.foodorderapp.activities.OrderDetailActivity.class);
            intent.putExtra("order", order);
            intent.putExtra("isAdmin", isAdmin);
            v.getContext().startActivity(intent);
        });

        if (isAdmin) {
            holder.layoutAdminActions.setVisibility(View.VISIBLE);
            holder.btnCancelOrder.setVisibility(View.GONE);

            holder.btnEnCours.setOnClickListener(v -> updateStatus(order, "En cours", v));
            holder.btnEnRoute.setOnClickListener(v -> updateStatus(order, "En route", v));
            holder.btnLivre.setOnClickListener(v -> updateStatus(order, "Livrée", v));
        } else {
            holder.layoutAdminActions.setVisibility(View.GONE);
            // Show cancel only if it's still "En cours"
            String currentStatus = order.getStatus() != null ? order.getStatus().trim() : "";
            if ("En cours".equalsIgnoreCase(currentStatus)) {
                holder.btnCancelOrder.setVisibility(View.VISIBLE);
                holder.btnCancelOrder.setOnClickListener(v -> {
                    // Ajout d'une confirmation pour l'annulation
                    new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                        .setTitle("Annuler la commande")
                        .setMessage("Voulez-vous vraiment annuler cette commande ?")
                        .setPositiveButton("Oui, annuler", (dialog, which) -> {
                            updateStatus(order, "Annulée", v);
                        })
                        .setNegativeButton("Non", null)
                        .show();
                });
            } else {
                holder.btnCancelOrder.setVisibility(View.GONE);
            }
        }
    }

    private void updateStatus(Order order, String newStatus, View v) {
        if (order == null || order.getId() == null || order.getId().isEmpty()) {
            Toast.makeText(v.getContext(), "Erreur : ID de commande manquant", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactiver le bouton pour éviter les clics multiples
        v.setEnabled(false);
        
        FirebaseFirestore.getInstance().collection("orders").document(order.getId())
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> {
                order.setStatus(newStatus);
                notifyDataSetChanged();
                Toast.makeText(v.getContext(), "Statut mis à jour : " + newStatus, Toast.LENGTH_SHORT).show();
                v.setEnabled(true);
            })
            .addOnFailureListener(e -> {
                v.setEnabled(true);
                Toast.makeText(v.getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal, tvOrderFoods;
        LinearLayout layoutAdminActions;
        Button btnEnCours, btnEnRoute, btnLivre, btnCancelOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderFoods = itemView.findViewById(R.id.tvOrderFoods);
            layoutAdminActions = itemView.findViewById(R.id.layoutAdminActions);
            btnEnCours = itemView.findViewById(R.id.btnEnCours);
            btnEnRoute = itemView.findViewById(R.id.btnEnRoute);
            btnLivre = itemView.findViewById(R.id.btnLivre);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
        }
    }
}
