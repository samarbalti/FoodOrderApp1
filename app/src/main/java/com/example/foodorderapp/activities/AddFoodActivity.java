package com.example.foodorderapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.example.foodorderapp.R;
import com.example.foodorderapp.models.Food;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;

public class AddFoodActivity extends AppCompatActivity {
    private EditText etFoodName, etFoodPrice, etFoodDescription, etFoodImageUrl;
    private Button btnSelectImage, btnAddFood, btnBackToMain;
    private ImageView imgPreview;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri selectedImageUri = null;
    private Bitmap cameraBitmap = null;
    private String existingFoodId = null;

    // Launcher pour galerie
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                cameraBitmap = null;
                etFoodImageUrl.setText(""); // Effacer l'URL manuelle si on choisit un fichier
                imgPreview.setImageURI(selectedImageUri);
                imgPreview.setVisibility(View.VISIBLE);
            }
        }
    );

    // Launcher pour camera
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bundle extras = result.getData().getExtras();
                if (extras != null) {
                    cameraBitmap = (Bitmap) extras.get("data");
                    selectedImageUri = null;
                    etFoodImageUrl.setText(""); // Effacer l'URL manuelle
                    imgPreview.setImageBitmap(cameraBitmap);
                    imgPreview.setVisibility(View.VISIBLE);
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        etFoodName = findViewById(R.id.etFoodName);
        etFoodPrice = findViewById(R.id.etFoodPrice);
        etFoodDescription = findViewById(R.id.etFoodDescription);
        etFoodImageUrl = findViewById(R.id.etFoodImageUrl);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnAddFood = findViewById(R.id.btnAddFood);
        btnBackToMain = findViewById(R.id.btnBackToMain);
        imgPreview = findViewById(R.id.imgPreview);

        // Mode Modification : Charger les données si un ID est passé
        existingFoodId = getIntent().getStringExtra("foodId");
        if (existingFoodId != null) {
            loadFoodData(existingFoodId);
            btnAddFood.setText("Enregistrer les modifications");
        }

        btnSelectImage.setOnClickListener(v -> showImagePickerDialog());
        btnAddFood.setOnClickListener(v -> addOrUpdateFood());
        btnBackToMain.setOnClickListener(v -> finish());

        // Preview en temps réel pour l'URL manuelle
        etFoodImageUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    // Si on entre une URL, on ignore l'image sélectionnée via galerie/caméra
                    selectedImageUri = null;
                    cameraBitmap = null;
                    
                    imgPreview.setVisibility(View.VISIBLE);
                    Glide.with(AddFoodActivity.this)
                        .load(url)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(imgPreview);
                } else if (selectedImageUri == null && cameraBitmap == null) {
                    imgPreview.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadFoodData(String foodId) {
        db.collection("foods").document(foodId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    etFoodName.setText(doc.getString("name"));
                    etFoodPrice.setText(String.valueOf(doc.getDouble("price")));
                    etFoodDescription.setText(doc.getString("description"));
                    String url = doc.getString("imageUrl");
                    etFoodImageUrl.setText(url);
                    
                    if (url != null && !url.isEmpty()) {
                        Glide.with(this)
                            .load(url)
                            .centerCrop()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_delete)
                            .into(imgPreview);
                        imgPreview.setVisibility(View.VISIBLE);
                    }
                }
            });
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choisir une image");
        String[] options = {"Camera", "Galerie", "Annuler"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else if (which == 1) {
                openGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(this, "Camera non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void addOrUpdateFood() {
        String name = etFoodName.getText().toString().trim();
        String priceStr = etFoodPrice.getText().toString().trim();
        String description = etFoodDescription.getText().toString().trim();
        String manualImageUrl = etFoodImageUrl.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Nom et prix obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Prix invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        String foodId = (existingFoodId != null) ? existingFoodId : db.collection("foods").document().getId();

        if (selectedImageUri != null || cameraBitmap != null) {
            uploadImageAndSaveFood(foodId, name, price, description);
        } else {
            // Utiliser l'URL existante si on ne change pas d'image en mode modification
            saveFoodToFirestore(foodId, name, price, manualImageUrl, description);
        }
    }

    private void uploadImageAndSaveFood(String foodId, String name, double price, String description) {
        StorageReference storageRef = storage.getReference().child("food_images/" + foodId + ".jpg");

        com.google.firebase.storage.UploadTask uploadTask;
        if (cameraBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            cameraBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            uploadTask = storageRef.putBytes(baos.toByteArray());
        } else {
            uploadTask = storageRef.putFile(selectedImageUri);
        }

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                saveFoodToFirestore(foodId, name, price, downloadUri.toString(), description);
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Inconnue";
                Toast.makeText(this, "Erreur upload : " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveFoodToFirestore(String foodId, String name, double price, String imageUrl, String description) {
        Food food = new Food(foodId, name, price, imageUrl, description);

        db.collection("foods").document(foodId).set(food)
            .addOnSuccessListener(aVoid -> {
                String msg = (existingFoodId != null) ? "Plat modifié !" : "Plat ajouté !";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
}
