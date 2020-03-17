package com.taocoder.eyewitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnFragmentChangeListener{

    private ProgressDialog progressDialog;
    private DrawerLayout drawerLayout;
    private SessionManager sessionManager;

    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 20);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 20);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 30);

        progressDialog = new ProgressDialog(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        sessionManager = new SessionManager(this);

        setupNavigation();
        changeFragment(new HomeFragment(), false);
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

    private void setupNavigation() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav);
        View view = navigationView.getHeaderView(0);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.navHome:
                        changeFragment(new HomeFragment(), true);
                        drawerLayout.closeDrawers();
                        return true;

                    case R.id.navContact:
                        changeFragment(new ContactFragment(), true);
                        drawerLayout.closeDrawers();
                        return true;

                    case R.id.navWitness:
                        changeFragment(new WitnessFragment(), true);
                        drawerLayout.closeDrawers();
                        return true;

                    case R.id.navAbout:
                        changeFragment(new AboutFragment(), true);
                        drawerLayout.closeDrawers();
                        return true;

                    case R.id.navLogout:
                        sessionManager.setLogin(false);
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                        return true;
                }

                item.setChecked(true);
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else
            super.onBackPressed();
    }

    @Override
    public void onFragmentChange(Fragment fragment) {
        changeFragment(fragment, true);
    }


    public void sendDialog(final String type, final String path) {


        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        ViewGroup viewGroup = findViewById(android.R.id.content);
        View view = getLayoutInflater().inflate(R.layout.send_layout, viewGroup, false);

        builder.setView(view);

        final TextView textView = (TextView) view.findViewById(R.id.text);
        textView.setText(getResources().getString(R.string.confirm));
        final MaterialButton open = (MaterialButton) view.findViewById(R.id.open);
        final MaterialButton cancel = (MaterialButton) view.findViewById(R.id.cancel);
        final TextInputEditText desc = (TextInputEditText) view.findViewById(R.id.desc);

        final AlertDialog dialog = builder.create();

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                description = desc.getText().toString();

                if (type.equalsIgnoreCase("video")) {

                    Utils.showMessage(getApplicationContext(), path);
                    new VideoUploader().execute(path);
                }
                else {
                    new PictureUploader().execute(path);
                }
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    class VideoUploader extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            String path = strings[0];

            if (path != null) {
                video(path);
            }
            else {
                Utils.showMessage(getApplicationContext(), "Path Error");
            }

            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }


    class PictureUploader extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            String path = strings[0];

            if (path != null) {
                picture(path);
            }
            else {
                Utils.showMessage(getApplicationContext(), "Path Error");
            }

            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }


    private void video(String path) {

        HttpURLConnection urlConnection = null;
        String lineEnd = "\r\n";
        String boundary = "*****";
        String hyphen = "--";
        DataOutputStream dataOutputStream;

        File file = new File(path);
        String[] info = path.split("/");
        String filename = info[info.length - 1];

        int read, available, size;
        byte[] buffer;
        int maxSize = 1024 * 1024;

        if (!file.isFile()) {

            Utils.showMessage(this, "File Does Not Exist ");
            return;
        }

        try {

            FileInputStream fileInputStream = new FileInputStream(file);
            java.net.URL url = new URL(Utils.BASE_URL + "upload-video");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            urlConnection.setRequestProperty("video", path);

            dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.writeBytes(hyphen + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"video\"; filename=\"" +
                    path + "\"" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);

            available = fileInputStream.available();
            size = Math.min(available, maxSize);

            buffer = new byte[size];
            read = fileInputStream.read(buffer, 0, size);

            while (read > 0) {
                dataOutputStream.write(buffer, 0, size);
                available = fileInputStream.available();
                size = Math.min(available, maxSize);
                read = fileInputStream.read(buffer, 0, size);
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(hyphen + boundary + lineEnd);
            dataOutputStream.flush();

            int response = urlConnection.getResponseCode();
            String message = urlConnection.getResponseMessage();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            if (response == 200) {

                JSONObject jsonObject = new JSONObject(builder.toString());

                if (jsonObject.getBoolean("error")) {
                    Utils.showMessage(this, jsonObject.getString("errorMessage"));
                }
                else {

                    JSONObject object = jsonObject.getJSONObject("data");
                    filename = object.getString("file_name");

                    saveDetails(filename);
                }
            }
            else {
                Utils.showMessage(this, "Internal Server Error. Please try again.");
            }

            fileInputStream.close();
            dataOutputStream.close();
            urlConnection.disconnect();



        }
        catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }
    }


    private void picture(String path) {

        HttpURLConnection urlConnection = null;
        String lineEnd = "\r\n";
        String boundary = "*****";
        String hyphen = "--";
        DataOutputStream dataOutputStream;
        File file = new File(path);
        String[] info = path.split("/");
        String filename = info[info.length - 1];

        int read, available, size;
        byte[] buffer;
        int maxSize = 1024 * 1024;

        if (!file.isFile()) {

            Utils.showMessage(this, "File Does Not Exist");
            return;
        }

        try {

            FileInputStream fileInputStream = new FileInputStream(file);
            java.net.URL url = new URL(Utils.BASE_URL + "upload-picture");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            urlConnection.setRequestProperty("logo", path);

            dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.writeBytes(hyphen + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"logo\"; filename=\"" +
                    path + "\"" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);

            available = fileInputStream.available();
            size = Math.min(available, maxSize);

            buffer = new byte[size];
            read = fileInputStream.read(buffer, 0, size);

            while (read > 0) {
                dataOutputStream.write(buffer, 0, size);
                available = fileInputStream.available();
                size = Math.min(available, maxSize);
                read = fileInputStream.read(buffer, 0, size);
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(hyphen + boundary + lineEnd);
            dataOutputStream.flush();

            int response = urlConnection.getResponseCode();
            String message = urlConnection.getResponseMessage();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            if (response == 200) {

                JSONObject jsonObject = new JSONObject(builder.toString());

                if (jsonObject.getBoolean("error")) {
                    Utils.showMessage(this, jsonObject.getString("errorMessage"));
                }
                else {

                    //Utils.showMessage(this, "Video Uploaded Successfully");
                    JSONObject object = jsonObject.getJSONObject("data");
                    filename = object.getString("file_name");

                    saveDetails(filename);
                }
            }
            else {
                Utils.showMessage(this, "Internal Server Error. Please try again.");
            }

            fileInputStream.close();
            dataOutputStream.close();
            urlConnection.disconnect();

        }
        catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }
    }

    private void saveDetails(final String filename) {

        final String email = sessionManager.getUsername();

        StringRequest request = new StringRequest(Request.Method.POST, Utils.BASE_URL + "witness", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("error")) {
                        Utils.showMessage(getApplicationContext(), jsonObject.getString("errorMessage"));
                    }
                    else {
                        //Utils.showMessage(getApplicationContext(), jsonObject.getString("message"));
                        responseDialog(jsonObject.getString("message"));
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("email", email);
                map.put("desc", description);
                map.put("filename", filename);

                return map;
            }
        };

        Controller.getInstance().addRequestQueue(request);
    }

    private void responseDialog(String message) {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getApplicationContext());
        dialog.setMessage(message);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        dialog.show();
    }
}