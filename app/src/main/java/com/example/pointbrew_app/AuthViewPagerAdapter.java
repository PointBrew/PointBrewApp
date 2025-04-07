package com.example.pointbrew_app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AuthViewPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 2;
    private static final int LOGIN_PAGE = 0;
    private static final int REGISTER_PAGE = 1;

    public AuthViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case LOGIN_PAGE:
                return LoginFragment.newInstance();
            case REGISTER_PAGE:
                return RegisterFragment.newInstance();
            default:
                return LoginFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
} 