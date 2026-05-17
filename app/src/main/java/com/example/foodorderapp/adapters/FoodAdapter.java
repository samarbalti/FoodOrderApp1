package com.example.foodorderapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import android.content.Intent;
import com.example.foodorderapp.activities.AddFoodActivity;
import com.example.foodorderapp.activities.FoodDetailActivity;
import com.example.foodorderapp.R;
import com.example.foodorderapp.models.Food;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private List<Food> foodList;
    private OnAddToCartListener addListener;
    private OnDeleteListener deleteListener;
    private boolean isAdmin;

    public interface OnAddToCartListener {
        void onAddToCart(Food food);
    }

    public interface OnDeleteListener {
        void onDelete(Food food);
    }

    public FoodAdapter(List<Food> foodList, OnAddToCartListener addListener, 
                      boolean isAdmin, OnDeleteListener deleteListener) {
        this.foodList = foodList;
        this.addListener = addListener;
        this.isAdmin = isAdmin;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.tvName.setText(food.getName());
        holder.tvPrice.setText(String.format("%.2f DT", food.getPrice()));

        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(food.getImageUrl())
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgFood);
        }

        if (isAdmin) {
            holder.btnAddToCart.setVisibility(View.GONE);
            holder.layoutAdminActions.setVisibility(View.VISIBLE);

            holder.btnEditFood.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), AddFoodActivity.class);
                intent.putExtra("foodId", food.getId());
                v.getContext().startActivity(intent);
            });

            holder.btnDeleteFood.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(food);
            });
        } else {
            holder.btnAddToCart.setVisibility(View.VISIBLE);
            holder.layoutAdminActions.setVisibility(View.GONE);
            holder.btnAddToCart.setOnClickListener(v -> {
                if (addListener != null) addListener.onAddToCart(food);
            });

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FoodDetailActivity.class);
                intent.putExtra("food", (android.os.Parcelable) food);
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood, btnEditFood, btnDeleteFood;
        TextView tvName, tvPrice;
        Button btnAddToCart;
        android.view.View layoutAdminActions;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvPrice = itemView.findViewById(R.id.tvFoodPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            layoutAdminActions = itemView.findViewById(R.id.layoutAdminActions);
            btnEditFood = itemView.findViewById(R.id.btnEditFood);
            btnDeleteFood = itemView.findViewById(R.id.btnDeleteFood);
        }
    }
}
