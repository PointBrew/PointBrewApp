package com.example.pointbrew_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin, btnGoogle;
    private TextView tvForgotPassword;
    
    private AuthManager authManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the AuthManager
        authManager = new AuthManager(requireContext());
        
        // Register the ActivityResultLauncher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK) {
                        Intent data = result.getData();
                        handleGoogleSignInResult(data);
                    } else {
                        // Google Sign In failed
                        Log.e(TAG, "Google Sign In failed. Result code: " + result.getResultCode());
                        Toast.makeText(getContext(), "Google Sign In failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        cbRememberMe = view.findViewById(R.id.cb_remember_me);
        btnLogin = view.findViewById(R.id.btn_login);
        btnGoogle = view.findViewById(R.id.btn_google);
        tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            
            if (validateInput(email, password)) {
                // Show loading
                btnLogin.setEnabled(false);
                
                // Perform login with email and password
                authManager.loginWithEmail(email, password, new AuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        // Login successful
                        btnLogin.setEnabled(true);
                        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainApp();
                    }

                    @Override
                    public void onError(Exception e) {
                        // Login failed
                        btnLogin.setEnabled(true);
                        Toast.makeText(getContext(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnGoogle.setOnClickListener(v -> {
            // Launch Google Sign-In flow
            googleSignInLauncher.launch(authManager.getGoogleSignInIntent());
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                etEmail.setError("Please enter your email address");
                etEmail.requestFocus();
                return;
            }
            
            // Send password reset email
            authManager.sendPasswordResetEmail(email, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to send reset email: " + task.getException().getMessage(), 
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
    private void handleGoogleSignInResult(Intent data) {
        authManager.handleGoogleSignInResult(data, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(getContext(), "Google Sign-In successful!", Toast.LENGTH_SHORT).show();
                navigateToMainApp();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }
    
    private void navigateToMainApp() {
        // This is where you'd navigate to your app's main activity
        // For now, just notify the activity that user is logged in
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onLoginSuccess();
        }
    }
} 