package imerir.scavengerhunt;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListeActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    protected static final String TAG = "Scavenger Hunt";

    ListView mListView;
    Button mSendButton;
    RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste);

        mListView = findViewById(R.id.itemList);
        mSendButton = findViewById(R.id.sendButton);

        //String[] item = getHttpRequest("toto");
        String[] item = {"toto", "titi"};

        // Create a List from String Array elements
        final List<String> item_list = new ArrayList<String>(Arrays.asList(item));

        // Create an ArrayAdapter from List
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, item_list);

        // DataBind ListView with items from ArrayAdapter
        mListView.setAdapter(arrayAdapter);

        File folder = new File(getFilesDir() + "/ScavengerHunt");
        Log.d(TAG, "toto "+getFilesDir());
        Log.d(TAG, ""+folder);

        if (folder.exists() == false) {
            boolean toto = folder.mkdirs();
            Log.d(TAG, "titi "+toto);
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                takePictureIntent();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchPager();
            }
        });


    }

    String[] getHttpRequest(String url) {
        String endpointUrl = url;

        Response.Listener<JSONObject> onSuccess = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String message = "";
                try {
                    JSONArray ducks = response.getJSONArray("images");
                    message = String.format("Il y a %d canards", ducks.length());

                    if (ducks.length() > 0) {
                        message += " et le 1er s'appelle " + ducks.getJSONObject(0).getString("name");
                    }

                } catch (Exception e) {
                    message = "Erreur de lecture du JSON";
                } finally {
                    Log.d(TAG, message);
                }
            }

        };

        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Erreur lors de la requête");
            }
        };

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpointUrl, null, onSuccess, onError);

        mRequestQueue.add(request);

        return new String[]{"toto", "titi"};
    }

    private void takePictureIntent() {
        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getFilesDir(), "/ScavengerHunt/Attachment" + ".jpg")));
        startActivityForResult(takePictureIntent, 1001);*/

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    void switchPager() {
        finish();

        Intent i = new Intent(ListeActivity.this, SendPicture.class);
        ListeActivity.this.startActivity(i);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "10");
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "80");
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
                return;
            }
            Log.d(TAG, "6");
            Location location = (Location) lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "7");
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            File f = new File(getFilesDir(),"/ScavengerHunt/Attachment" + ".jpg");
            geoTag(f.getAbsolutePath(),latitude,longitude);
            Log.d(TAG, "8");
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
