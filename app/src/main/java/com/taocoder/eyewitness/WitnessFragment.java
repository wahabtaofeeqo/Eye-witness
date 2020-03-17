package com.taocoder.eyewitness;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class WitnessFragment extends Fragment {

    private OnFragmentChangeListener listener;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private List<Witness> witnesses;
    private WitnessAdapter adapter;


    public WitnessFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (OnFragmentChangeListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_witness, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.loading);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);

        witnesses = new ArrayList<>();
        adapter = new WitnessAdapter(getContext(), witnesses, listener);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        final SessionManager sessionManager = new SessionManager(getContext());
        getWitness(sessionManager.getUsername());
        return view;
    }

    private void getWitness(final String email) {
        StringRequest request = new StringRequest(Request.Method.POST, Utils.BASE_URL + "my-witnesses", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideBar();

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("error")) {
                        Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                    }
                    else {

                        recyclerView.setVisibility(View.VISIBLE);
                        JSONArray data = jsonObject.getJSONArray("data");

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject row = data.getJSONObject(i);

                            Witness witness = new Witness();
                            witness.setId(row.getInt("id"));
                            witness.setDesc(row.getString("description"));
                            witness.setFilename(row.getString("filename"));
                            witness.setType(row.getString("type"));
                            witness.setDate(row.getString("created"));

                            witnesses.add(witness);
                        }

                        adapter.notifyDataSetChanged();
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                   hideBar();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               hideBar();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("email", email);

                return map;
            }
        };

        Controller.getInstance().addRequestQueue(request);
    }

    private void hideBar() {
        if (progressBar != null && progressBar.isShown()) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
