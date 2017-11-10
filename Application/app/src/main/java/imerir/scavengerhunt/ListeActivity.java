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
import android.support.v4.content.FileProvider;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ListeActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    protected static final String TAG = "Scavenger Hunt";
    String mCurrentPhotoPath;

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

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d(TAG, ex.toString());

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                galleryAddPic();
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        }

    }

    void switchPager() {
        finish();

        Intent i = new Intent(ListeActivity.this, SendPicture.class);
        ListeActivity.this.startActivity(i);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
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

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
