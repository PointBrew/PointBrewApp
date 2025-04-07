package com.example.pointbrew_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";

    private TextInputEditText etDisplayName, etBirthDate, etEmail, etPassword;
    private Button btnRegister, btnGoogle;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    
    private AuthManager authManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
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
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        
        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        etDisplayName = view.findViewById(R.id.et_display_name);
        etBirthDate = view.findViewById(R.id.et_birth_date);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnRegister = view.findViewById(R.id.btn_register);
        btnGoogle = view.findViewById(R.id.btn_google);
    }

    private void setupListeners() {
        etBirthDate.setOnClickListener(v -> showDatePickerDialog());

        btnRegister.setOnClickListener(v -> {
            String displayName = etDisplayName.getText().toString().trim();
            String birthDate = etBirthDate.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            
            if (validateInput(displayName, birthDate, email, password)) {
                // Show loading
                btnRegister.setEnabled(false);
                
                try {
                    Date birthDateObj = dateFormat.parse(birthDate);
                    
                    // Register with email and password
                    authManager.registerWithEmail(displayName, email, password, birthDateObj, new AuthManager.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            btnRegister.setEnabled(true);
                            Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                            navigateToMainApp();
                        }

                        @Override
                        public void onError(Exception e) {
                            btnRegister.setEnabled(true);
                            Toast.makeText(getContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (ParseException e) {
                    btnRegister.setEnabled(true);
                    Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGoogle.setOnClickListener(v -> {
            // Launch Google Sign-In flow
            googleSignInLauncher.launch(authManager.getGoogleSignInIntent());
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

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateInView();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set maximum date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        
        datePickerDialog.show();
    }

    private void updateDateInView() {
        etBirthDate.setText(dateFormat.format(calendar.getTime()));
    }

    private boolean validateInput(String displayName, String birthDate, String email, String password) {
        if (displayName.isEmpty()) {
            etDisplayName.setError("Name is required");
            etDisplayName.requestFocus();
            return false;
        }

        if (birthDate.isEmpty()) {
            etBirthDate.setError("Birth date is required");
            etBirthDate.requestFocus();
            return false;
        }

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
        
        // Password length validation
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }
    
    private void navigateToMainApp() {
        // This is where you'd navigate to your app's main activity
        // For now, just notify the activity that user is registered
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onLoginSuccess();
        }
    }
}