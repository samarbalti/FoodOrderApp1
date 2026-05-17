service cloud.firestore {
  match /databases/{database}/documents {
    
    // Autoriser l'utilisateur à lire et écrire son propre profil dans la collection "users"
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Autoriser la lecture des plats pour tout le monde, mais écriture seulement pour les admins
    match /foods/{foodId} {
      allow read: if true;
      allow write: if request.auth != null && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // Autoriser les utilisateurs connectés à voir et créer leurs propres commandes
    match /orders/{orderId} {
      allow create: if request.auth != null;
      allow read: if request.auth != null && (resource.data.userId == request.auth.uid || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin');
      allow update, delete: if request.auth != null && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
  }
}