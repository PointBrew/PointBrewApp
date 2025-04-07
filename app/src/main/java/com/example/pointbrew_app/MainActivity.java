package com.example.pointbrew_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvHeader, tvSubheader;
    private AuthViewPagerAdapter pagerAdapter;
    private AuthManager authManager;
    
    private final String[] tabTitles = new String[]{"Log In", "Sign Up"};
    private final String[] headerTitles = new String[]{"Login", "Register"};
    private final String[] subheaderTitles = new String[]{
            "Enter your credentials to login", 
            "Create a new account"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize AuthManager
        authManager = new AuthManager(this);
        
        // Check if user is already logged in
        if (authManager.isUserLoggedIn()) {
            // User is already logged in, navigate to main app screen
            navigateToMainApp();
            return;
        }
        
        initViews();
        setupViewPager();
        setupTabLayout();
    }
    
    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        tvHeader = findViewById(R.id.tv_header);
        tvSubheader = findViewById(R.id.tv_subheader);
    }
    
    private void setupViewPager() {
        pagerAdapter = new AuthViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateHeaderText(position);
            }
        });
    }
    
    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
    
    private void updateHeaderText(int position) {
        tvHeader.setText(headerTitles[position]);
        tvSubheader.setText(subheaderTitles[position]);
    }
    
    /**
     * Called when login or registration is successful
     */
    public void onLoginSuccess() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            String welcomeMsg = "Welcome" + (displayName != null ? ", " + displayName : "!");
            Toast.makeText(this, welcomeMsg, Toast.LENGTH_SHORT).show();
            navigateToMainApp();
        }
    }
    
    /**
     * Navigate to the main app screen after successful login/registration
     */
    private void navigateToMainApp() {
        // For demonstration purposes, we'll just show a toast
        // In a real app, you would navigate to your app's main activity
        Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_SHORT).show();
        
        // Example of navigation to a new activity
        // Intent intent = new Intent(this, HomeActivity.class);
        // startActivity(intent);
        // finish(); // Finish this activity so user can't go back to the login screen
    }
}