package com.taocoder.eyewitness;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements Validator.ValidationListener {


    @Email
    private TextInputEditText username;

    @NotEmpty
    private TextInputEditText password;

    private Validator validator;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;

    private OnFragmentChangeListener listener;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        validator = new Validator(this);
        progressDialog = new ProgressDialog(getContext());
        sessionManager = new SessionManager(getContext());
        validator.setValidationListener(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (OnFragmentChangeListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        username = (TextInputEditText) view.findViewById(R.id.username);
        password = (TextInputEditText) view.findViewById(R.id.password);

        MaterialButton login = (MaterialButton) view.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validator.validate();
            }
        });

        MaterialButton reg = (MaterialButton) view.findViewById(R.id.register);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onFragmentChange(new RegisterFragment());
            }
        });

        return view;
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        Utils.showMessage(getContext(), "Provide Valid Data");
    }

    @Override
    public void onValidationSucceeded() {
        login(username.getText().toString(), password.getText().toString());
    }

    private void login(final String email, final String password) {

        if (password != null) {

            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.BASE_URL + "login", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getBoolean("error")) {
                            Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                        }
                        else {

                            sessionManager.setLogin(true);
                            sessionManager.setUsername(email);
                            Utils.showMessage(getContext(), jsonObject.getString("message"));
                            startActivity(new Intent(getContext(), MainActivity.class));
                            getActivity().finish();
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                    }
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();

                            Utils.showMessage(getContext(), error.getMessage());
                            progressDialog.dismiss();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> map = new HashMap<>();
                    map.put("email", email);
                    map.put("password", password);
                    return  map;
                }
            };

            Controller.getInstance().addRequestQueue(stringRequest);
        }
    }
}