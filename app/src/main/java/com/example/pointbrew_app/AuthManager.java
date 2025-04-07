package com.example.pointbrew_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    private static final String TAG = "AuthManager";
    public static final int RC_SIGN_IN = 9001;

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mFirestore;
    private final GoogleSignInClient mGoogleSignInClient;
    private final Context mContext;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(Exception e);
    }

    public AuthManager(Context context) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    public Intent getGoogleSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }

    public void signOut(AuthCallback callback) {
        // Sign out from Firebase
        mAuth.signOut();
        
        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void handleGoogleSignInResult(Intent data, AuthCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken(), callback);
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            callback.onError(e);
        }
    }

    private void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) mContext, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToFirestore(user, null, null, true);
                        callback.onSuccess(user);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    public void registerWithEmail(String displayName, String email, String password, Date birthDate, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) mContext, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToFirestore(user, displayName, birthDate, false);
                        callback.onSuccess(user);
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    public void loginWithEmail(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) mContext, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String displayName, Date birthDate, boolean isGoogleSignIn) {
        if (user == null) return;

        // Create user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("uid", user.getUid());
        userData.put("createdAt", new Date());
        
        if (isGoogleSignIn) {
            // For Google Sign-In, use the display name from Google account
            userData.put("displayName", user.getDisplayName());
            userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
            userData.put("isGoogleSignIn", true);
        } else {
            // For Email/Password registration, use the provided display name
            userData.put("displayName", displayName);
            userData.put("birthDate", birthDate);
            userData.put("isGoogleSignIn", false);
        }

        // First check if user document already exists to avoid overwriting existing data
        DocumentReference userRef = mFirestore.collection("users").document(user.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    // Document doesn't exist, create it
                    userRef.set(userData)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "User data saved to Firestore"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error saving user data", e));
                } else {
                    // Document exists, update login timestamp
                    userRef.update("lastLoginAt", new Date())
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "User last login updated"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error updating last login", e));
                }
            } else {
                Log.w(TAG, "Error checking if user exists", task.getException());
            }
        });
    }

    public void sendPasswordResetEmail(String email, OnCompleteListener<Void> listener) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(listener);
    }
} 