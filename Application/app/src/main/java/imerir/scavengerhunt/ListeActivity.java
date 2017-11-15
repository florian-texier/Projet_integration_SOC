package imerir.scavengerhunt;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListeActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    protected static final String TAG = "Scavenger Hunt";
    String contURl = "http://172.30.1.35:5000";

    ListView mListView;
    Button mSendButton;
    RequestQueue mRequestQueue;
    String mCurrentPhotoPath;
    File photoFile = null;
    List<String> item_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste);

        mRequestQueue = Volley.newRequestQueue(this);

        mListView = findViewById(R.id.itemList);
        mSendButton = findViewById(R.id.sendButton);
        item_list = new ArrayList<>();

        getHttpRequest(contURl+"/liste");


        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchPager();
            }
        });


    }

    void getHttpRequest(String url) {

        Response.Listener<JSONObject> onSuccess = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG,response.toString());
                try {
                    JSONArray liste = response.getJSONObject("message").getJSONArray("liste");

                    item_list.clear();

                    for (int i =0;i<liste.length();i++){
                        item_list.add(liste.getJSONObject(i).getJSONObject("value").getJSONObject("name").getString("en"));

                    }

                    if (!item_list.isEmpty()){
                        // Create an ArrayAdapter from List
                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListeActivity.this, android.R.layout.simple_list_item_1, item_list);

                        // DataBind ListView with items from ArrayAdapter
                        mListView.setAdapter(arrayAdapter);

                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                                String name =mListView.getItemAtPosition(position).toString();
                                takePictureIntent(name);
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.d(TAG,e.getMessage());
                }
            }

        };

        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Erreur lors de la requête");
            }
        };

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, onSuccess, onError);

        mRequestQueue.add(request);

    }

    private void takePictureIntent(String name) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile(name);
            } catch (IOException ex) {
                Log.d(TAG, ex.toString());

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        }

    }

    private File createImageFile(String name) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String imageFileName = "JPEG_" + timeStamp + "_";
        String imageFileName = name+"_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    void switchPager() {
        finish();

        Intent i = new Intent(ListeActivity.this, SendPicture.class);
        ListeActivity.this.startActivity(i);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
                return;
            }
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            geoTag(photoFile.getAbsolutePath(),latitude,longitude);
        }
    }

    public void geoTag(String filename, double latitude, double longitude){
        ExifInterface exif;

        try {
            exif = new ExifInterface(filename);
            int num1Lat = (int)Math.floor(latitude);
            int num2Lat = (int)Math.floor((latitude - num1Lat) * 60);
            double num3Lat = (latitude - ((double)num1Lat+((double)num2Lat/60))) * 3600000;

            int num1Lon = (int)Math.floor(longitude);
            int num2Lon = (int)Math.floor((longitude - num1Lon) * 60);
            double num3Lon = (longitude - ((double)num1Lon+((double)num2Lon/60))) * 3600000;

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat+"/1,"+num2Lat+"/1,"+num3Lat+"/1000");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon+"/1,"+num2Lon+"/1,"+num3Lon+"/1000");


            if (latitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            }

            if (longitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            }

            exif.saveAttributes();
        } catch (IOException e) {
            Log.e("PictureActivity", e.getLocalizedMessage());
        }

    }


}
