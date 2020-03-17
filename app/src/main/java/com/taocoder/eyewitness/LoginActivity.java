package com.taocoder.eyewitness;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

public class LoginActivity extends AppCompatActivity implements OnFragmentChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else {
            changeFragment(new LoginFragment(), false);
        }
    }

    public void changeFragment(Fragment fragment, boolean backTrack) {

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.container, fragment);

        if (backTrack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    @Override
    public void onFragmentChange(Fragment fragment) {
        changeFragment(fragment, true);
    }
}