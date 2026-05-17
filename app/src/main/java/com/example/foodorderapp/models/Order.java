package com.example.foodorderapp.models;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private String id;
    private String userId;
    private String userName;
    private String userAddress;
    private String userPhone;
    private List<Food> items;
    private double totalPrice;
    private String status; // "En cours", "En route", "Livrée", "Annulée"
    private Object timestamp;

    public Order() {} // Requis pour Firebase

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserAddress() { return userAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public List<Food> getItems() { return items; }
    public void setItems(List<Food> items) { this.items = items; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) {
        if (timestamp instanceof com.google.firebase.Timestamp) {
            this.timestamp = ((com.google.firebase.Timestamp) timestamp).toDate().getTime();
        } else {
            this.timestamp = timestamp;
        }
    }
}
