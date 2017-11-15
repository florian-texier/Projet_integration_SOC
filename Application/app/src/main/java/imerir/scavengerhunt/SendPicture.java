package imerir.scavengerhunt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.altbeacon.beacon.BeaconManager;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static imerir.scavengerhunt.MainActivity.PREFS;

public class SendPicture extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    protected static final String TAG = "Scavenger Hunt";

    private BeaconManager beaconManager;
    TextView mDistance;
    Button select;
    Button sendPicture;
    ImageView image;
    RequestQueue mRequestQueue;

    String contURl = "https://routerint.mignolet.fr";

    File imgFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_picture);
        select = findViewById(R.id.selectpicture);
        sendPicture = findViewById(R.id.button2);
        image = findViewById(R.id.imageView);

        mRequestQueue = Volley.newRequestQueue(this);


        sendPicture.setEnabled(false);

        sendPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                httpPostImage(contURl+"/");

            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showFileChooser();

            }
        });
    }

    private void showFileChooser() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");      //all files
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(SendPicture.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    try {
                        path = getPath(this, uri);
                    } catch (URISyntaxException e) {
                        Log.d(TAG, e.getMessage());                    }
                    Log.d(TAG, "File Path: " + path);

                    imgFile = new File(path);

                    if(imgFile.exists()){

                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                        image.setImageBitmap(myBitmap);

                        sendPicture.setEnabled(true);

                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    void httpPostImage(String url) {

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String deviceID = prefs.getString("DeviceID", null);

        String latitute = "";
        String longitude = "";
        try {
            final ExifInterface exifInterface = new ExifInterface(imgFile.getAbsolutePath());
            latitute = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

        } catch (IOException e) {
            Log.e("TAG", e.getMessage());
        }

        String image64 = jpegTo64();


        JSONObject postData = new JSONObject();
        try {
            postData.put("id_Equipe", deviceID);
            postData.put("latitute", latitute);
            postData.put("longitude", longitude);
            postData.put("image", image64);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }

        Response.Listener<JSONObject> onSuccess = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Toast toast = Toast.makeText(SendPicture.this, "Photo analysée ! Vérifiez l'afficheur !", Toast.LENGTH_LONG);
                    toast.show();
                } catch (Exception e) {
                    Toast toast = Toast.makeText(SendPicture.this, "Erreur de lecture de l'image", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        };

        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(SendPicture.this, "Erreur lors de l'envoi de l'image", Toast.LENGTH_LONG);
                toast.show();
                Log.e("Message Recu :","Erreur lors de la requête");
            }
        };



        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData, onSuccess, onError);
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(request);
    }

    public String jpegTo64(){
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

}
