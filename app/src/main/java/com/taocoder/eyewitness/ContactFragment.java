package com.taocoder.eyewitness;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment implements Validator.ValidationListener {

    private MainActivity activity;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    private Validator validator;

    @NotEmpty
    private TextInputEditText subject;

    @NotEmpty
    private TextInputEditText message;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();
        sessionManager = new SessionManager(getContext());
        progressDialog = new ProgressDialog(getActivity());

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        subject = (TextInputEditText) view.findViewById(R.id.subject);
        message = (TextInputEditText) view.findViewById(R.id.message);

        final MaterialButton button = (MaterialButton) view.findViewById(R.id.send);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validator.validate();
            }
        });

        return view;
    }

    @Override
    public void onValidationSucceeded() {

        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        contact(subject.getText().toString(), message.getText().toString());
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        Utils.showMessage(activity, "Provide The Required Data");
    }

    private void contact(final String sub, final String msg) {

        final String email = sessionManager.getUsername();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.BASE_URL + "contact", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("error")) {
                        Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                    }
                    else {

                        subject.setText("");
                        message.setText("");

                        responseDialog(jsonObject.getString("message"));
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("email", email);
                map.put("subject", sub);
                map.put("message", msg);

                return  map;
            }
        };

        Controller.getInstance().addRequestQueue(stringRequest);
    }

    private void responseDialog(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage(message);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.changeFragment(new HomeFragment(), false);
            }
        });

        dialog.show();
    }
}

