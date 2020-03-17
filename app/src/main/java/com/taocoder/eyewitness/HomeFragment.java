package com.taocoder.eyewitness;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private BottomSheetDialog dialog;
    private MainActivity activity;

    private ProgressDialog progressDialog;
    private SessionManager sessionManager;

    private String[] permissions = new String[] {Manifest.permission.RECORD_AUDIO};

    private boolean audioGranted = false;

    private String filename = null;
    private MediaRecorder recorder;

    private OnFragmentChangeListener listener;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        final MaterialButton video = (MaterialButton) view.findViewById(R.id.video);
        //imageView = (AppCompatImageView) view.findViewById(R.id.logo);

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startVideo();
                startDialog();
            }
        });

        checkPermission();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();
        progressDialog = new ProgressDialog(getContext());
        sessionManager = new SessionManager(getContext());

        filename = getActivity().getExternalCacheDir().getAbsolutePath();
        filename += "/witness.3gp";
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (OnFragmentChangeListener) getActivity();
    }

    private void startVideo() {

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, Utils.VIDEO_REQUEST_CODE);
        }
        else {
            Utils.showMessage(getContext(), "No Suitable app for the action");
        }
    }

    private void startPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {

//            String path = getContext().getFilesDir().getAbsolutePath() + "/witness.jpg";
//            File file = new File(path);
//
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(intent, Utils.PICTURE_REQUEST_CODE);
        }
        else {
            Utils.showMessage(getContext(), "No suitable app for the action");
        }
    }

    private void startDialog() {

        View view = getLayoutInflater().inflate(R.layout.dialog, null);

        List<String> list = new ArrayList<>();
        list.add("Video Witness");
        list.add("Audio Witness");
        list.add("Picture Witness");
        list.add("Text Witness");
        list.add("Cancel");

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new InnerAdapter(getContext(), list));

        dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(view);
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Utils.AUDIO_PERMISSION:
               audioGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
               if (audioGranted)
                   startAudio();
               break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Utils.VIDEO_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null && data.getData() != null) {
                        String path = FilePath.getPath(getContext(), data.getData());

                        if (path != null) {

                            progressDialog.setMessage("Uploading...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            new VideoUploader().execute(path);
                        }
                    }
                }
                break;

            case Utils.PICTURE_REQUEST_CODE:

                if (resultCode == Activity.RESULT_OK) {

                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                    Uri uri = getImageUri(getContext(), bitmap);
                    String path = FilePath.getPath(getContext(), uri);
                    if (path != null) {

                        progressDialog.setMessage("Uploading...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        new PictureUploader().execute(path);
                    }

                }
            break;

                case Utils.AUDIO_REQUEST_CODE:
                    if (data != null && data.getData() != null) {
                        Uri uri = data.getData();

                        String path = FilePath.getPath(getContext(), uri);
                        if (path != null) {

                            progressDialog.setMessage("Uploading...");
                            //progressDialog.setCancelable(false);
                            progressDialog.show();

                            new AudioUploader().execute(path);
                            Log.i("RES", path);
                        }
                    }
                   break;
        }
    }

    private Uri getImageUri(Context applicationContext, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
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
                Utils.showMessage(getContext(), "Path Error");
            }

            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }


    class AudioUploader extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            String path = strings[0];

            if (path != null) {
                audio(path);
            }
            else {
                Utils.showMessage(getContext(), "Path Error");
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
                Utils.showMessage(getContext(), "Path Error");
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

            Utils.showMessage(getContext(), "File Does Not Exist ");
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
                    Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                }
                else {

                    JSONObject object = jsonObject.getJSONObject("data");
                    filename = object.getString("file_name");

                    saveDetails(filename, "video");
                }
            }
            else {
                Utils.showMessage(getContext(), "Internal Server Error. Please try again.");
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

    private void audio(String path) {

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

            Utils.showMessage(getContext(), "File Does Not Exist ");
            return;
        }

        try {

            FileInputStream fileInputStream = new FileInputStream(file);
            java.net.URL url = new URL(Utils.BASE_URL + "upload-audio");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            urlConnection.setRequestProperty("audio", path);

            dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.writeBytes(hyphen + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"audio\"; filename=\"" +
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

                    Log.i("RES", builder.toString());
                    Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                }
                else {

                    JSONObject object = jsonObject.getJSONObject("data");
                    filename = object.getString("file_name");

                    saveDetails(filename, "audio");
                }
            }
            else {
                Utils.showMessage(getContext(), "Internal Server Error. Please try again.");
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

            Utils.showMessage(getContext(), "File Does Not Exist");
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

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            if (response == 200) {

                JSONObject jsonObject = new JSONObject(builder.toString());

                if (jsonObject.getBoolean("error")) {
                    Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                }
                else {

                    JSONObject object = jsonObject.getJSONObject("data");
                    filename = object.getString("file_name");

                    saveDetails(filename, "picture");
                }
            }
            else {
                Utils.showMessage(getContext(), "Internal Server Error. Please try again.");
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

    private void saveDetails(final String filename, final String type) {

        final String email = sessionManager.getUsername();

        StringRequest request = new StringRequest(Request.Method.POST, Utils.BASE_URL + "witness", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("error")) {
                        Utils.showMessage(getContext(), jsonObject.getString("errorMessage"));
                    }
                    else {
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
                map.put("filename", filename);
                map.put("type", type);

                return map;
            }
        };

        Controller.getInstance().addRequestQueue(request);
    }

    private void responseDialog(String message) {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getContext());
        dialog.setMessage(message);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        dialog.show();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            audioGranted = true;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.RECORD_AUDIO}, Utils.AUDIO_PERMISSION);
    }

    class InnerAdapter extends RecyclerView.Adapter<InnerAdapter.ListHolder> {

        List<String> strings;

        public InnerAdapter(Context context, List<String> list) {
            strings = list;
        }

        public class ListHolder extends RecyclerView.ViewHolder {

            public TextView textView;
            public ListHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.textView);
            }
        }

        @NonNull
        @Override
        public InnerAdapter.ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list, parent, false);
            return new ListHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InnerAdapter.ListHolder holder, final int position) {
            holder.textView.setText(strings.get(position));
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (strings.get(position).equalsIgnoreCase("cancel")) {
                        dialog.dismiss();
                    }

                    if (strings.get(position).equalsIgnoreCase("Video Witness")) {
                        dialog.dismiss();
                        startVideo();
                    }

                    if (strings.get(position).equalsIgnoreCase("Audio Witness")) {
                        dialog.dismiss();

//                        if (audioGranted) {
//
//                        }
//                        else
//                            requestAudioPermission();
                        startAudio();
                    }

                    if (strings.get(position).equalsIgnoreCase("Picture Witness")) {
                        dialog.dismiss();
                        startPicture();
                    }

                    if (strings.get(position).equalsIgnoreCase("Text Witness")) {
                        dialog.dismiss();

                        listener.onFragmentChange(new TextFragment());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return strings.size();
        }
    }

    private void startAudio() {

        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, Utils.AUDIO_REQUEST_CODE);
        }
        else {
            Utils.showMessage(getContext(), "No Suitable app for the action");
        }
    }
}