package com.taocoder.eyewitness;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class IndexFragment extends Fragment {


    private OnFragmentChangeListener listener;

    public IndexFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_index, container, false);

        final MaterialButton button = (MaterialButton) view.findViewById(R.id.start);
        final SessionManager sessionManager = new SessionManager(getContext());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sessionManager.isFirstTime()) {
                    listener.onFragmentChange(new TermsFragment());
                }
                else {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (OnFragmentChangeListener) getActivity();
    }
}
