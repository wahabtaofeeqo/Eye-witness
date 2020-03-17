package com.taocoder.eyewitness;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class TermsFragment extends Fragment {


    public TermsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_terms, container, false);

        final SessionManager sessionManager = new SessionManager(getContext());
        MaterialButton button = (MaterialButton) view.findViewById(R.id.accept);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sessionManager.setIsFirstTime(false);

//                if (sessionManager.isLoggedIn()) {
//                    startActivity(new Intent(getContext(), MainActivity.class));
//                }
//                else {
//
//                }

                startActivity(new Intent(getContext(), LoginActivity.class));
            }
        });

        return view;
    }

}
