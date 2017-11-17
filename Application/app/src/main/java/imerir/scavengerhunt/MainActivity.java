package imerir.scavengerhunt;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import static java.lang.String.format;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final int REQUEST_PERMISSION = 1;
    protected static final String TAG = "Scavenger Hunt";
    static final int REQUEST_PERMCAM = 1;
    public static final String PREFS = "PREFS";
    SharedPreferences sharedPreferences;
    public static final String contURl = "http://51.254.121.94:4000";
    //public static final String contURl = "https://routerint.mignolet.fr";
    //public static final String contURl = "http://172.30.1.35:5003";

    String id = null;
    String id2 = null;
    Region region1;
    Region region2;
    infoPeriph infoPeri;
    boolean inscrit = false;
    int typeRegion = 1;
    double distSendBeacon;

    private BeaconManager beaconManager;
    RequestQueue mRequestQueue;

    TextView mTextView;
    TextView mDistance;


    void displayText(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(message);
            }
        });
    }

    // Model Components
    public static BarcodeDetector mDetector;
    public static CameraSource mCamera;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.stop();
        mDetector.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoPeri = infoPeriph.getInstance();

        mRequestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);

        mTextView = findViewById(R.id.textView);
        mDistance = findViewById(R.id.distance);

        postHttpRequest(contURl+"/team");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMCAM);
                setupDetectorAndCamera();
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showAlertDialog("This app needs location access",
                        "Please grant location access so this app can detect beacons.", new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
                            }
                        });
            }
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                showAlertDialog("This app needs location access",
                        "Please grant location access so this app can detect beacons.", new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSION);
                            }
                        });
            }
        } else {
            setupDetectorAndCamera();
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    showAlertDialog("Functionality limited",
                            "Since location access has not been granted, this app will not be able to discover beacons when in the background.", null);
                }
                return;
            }
        }
        if (requestCode == REQUEST_PERMCAM) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupDetectorAndCamera(); // continue the setup
            } else {
                Log.e(TAG, "Permission was denied or request was cancelled.");
                displayText("Access to the camera is REQUIRED.");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        beaconManager = BeaconManager.getInstanceForApplication(this);

        final String iBeaconLayout = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(iBeaconLayout));

        beaconManager.bind(this);
        postHttpRequest(contURl+"/team");

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showAlertDialog(String title, String message, DialogInterface.OnDismissListener onDismissListener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        if (onDismissListener != null) builder.setOnDismissListener(onDismissListener);
        builder.show();
    }

    @Override
    public void onBeaconServiceConnect() {

        Log.i(TAG, "beaconServiceConnected");
        beaconManager.addRangeNotifier(new RangeNotifier() {

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                for (Beacon beacon : collection) {
                    Log.i(TAG, "Detected beacon : " + beacon.getId1());
                    Log.i(TAG, "Detected beacon @ distance " + beacon.getDistance());
                    if (typeRegion == 1){
                        final double dist = beacon.getDistance();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDistance.setTextColor(Color.RED);
                                mDistance.setText(format("%.2f", dist) + " m");

                            }
                        });
                        if (inscrit){
                            getHttpRequest(contURl+"/beaconSendPicture",2);
                            switchPager();
                        }
                        else{
                            if (dist < 3.0){
                                postHttpRequest(contURl+"/inscript");
                                getHttpRequest(contURl+"/beaconSendPicture",2);
                                postHttpRequest(contURl+"/team");
                                switchPager();
                            }
                        }
                    }
                    else{
                        distSendBeacon = beacon.getDistance();
                        infoPeri.setDistance(distSendBeacon);

                    }


                }
            }
        });
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "didEnterRegion");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "didExitRegion");
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.i(TAG, "didDetermineStateForRegion = " + i);
            }
        });
        getHttpRequest(contURl+"/beaconInscript",1);

    }

    void setupDetectorAndCamera() {
        // build the detector
        mDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        mDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                processBarcodes(barcodes);
            }
        });

        if (!mDetector.isOperational()) {
            mTextView.setText("Could not set up the detector!\nPlease update or upgrade your tablet (consider an iPhone X).");
            return;
        }

        // build the camera
        CameraSource.Builder cameraBuilder = new CameraSource.Builder(this, mDetector);
        cameraBuilder.setAutoFocusEnabled(true);
        cameraBuilder.setFacing(CameraSource.CAMERA_FACING_BACK);
        cameraBuilder.setRequestedFps(10);

        mCamera = cameraBuilder.build();

        // start the camera
        try {
            mCamera.start();
        } catch (SecurityException e) { // camera not allowed
            Log.e(TAG, e.getMessage());
            mTextView.setText("Failed to start the camera");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            mTextView.setText("Failed to start the camera");
        }
    }

    void processBarcodes(SparseArray<Barcode> barcodes) {
        if (barcodes.size() != 0) {
            String url = barcodes.valueAt(0).displayValue;
            displayText(url);
            if (inscrit){
                getHttpRequest(contURl+"/beaconSendPicture",2);
                switchPager();
            }
            else{

                postHttpRequest(contURl+"/inscript");
                getHttpRequest(contURl+"/beaconSendPicture",2);
                postHttpRequest(contURl+"/team");
                switchPager();

            }
        } else {
            displayText("No barcode detected.");
        }
    }

    void getHttpRequest(String url,final int requestType) {
        Log.i(TAG,url);

        Response.Listener<JSONObject> onSuccess = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, response.toString());
                try {
                    if (requestType == 1){
                        typeRegion =1;
                        id = response.getJSONObject("message").getString("uid");
                        region1 = new Region("Totorama", Identifier.parse(id), null, null);
                        beaconManager.startMonitoringBeaconsInRegion(region1);
                        beaconManager.startRangingBeaconsInRegion(region1);
                    }
                    if (requestType == 2){
                        typeRegion = 2;
                        id2 = response.getJSONObject("message").getString("uid");
                        region2 = new Region("Totorama", Identifier.parse(id), null, null);
                        beaconManager.stopMonitoringBeaconsInRegion(region1);
                        beaconManager.stopRangingBeaconsInRegion(region1);
                        beaconManager.startMonitoringBeaconsInRegion(region2);
                        beaconManager.startRangingBeaconsInRegion(region2);
                    }


                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

        };

        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    Log.e(TAG,error.getMessage());
                    Log.i(TAG,"Erreur lors de la requête");
                }catch (NullPointerException e){
                    Log.e(TAG,e.getMessage());
                }

            }
        };

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, onSuccess, onError);

        mRequestQueue.add(request);

    }

    void postHttpRequest(final String url) {
        Log.i(TAG,url);
        JSONObject postData = new JSONObject();
        try {
            postData.put("id", getDeviceId(this));
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }

        Response.Listener<JSONObject> onSuccess = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i(TAG,response.toString());
                    if (url.contains("team")){
                        inscrit = true;
                    }

                } catch (Exception e) {
                    Log.e(TAG,e.getMessage());
                }
            }
        };

        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try{
                    Log.e(TAG,error.getMessage());
                    Log.i(TAG,"Erreur lors de la requête");
                }catch (NullPointerException e){
                    Log.e(TAG,e.getMessage());
                }
            }
        };

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData, onSuccess, onError);

        mRequestQueue.add(request);
    }

    public String getDeviceId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID) + Build.SERIAL;
        Log.d(TAG,androidId);

        sharedPreferences.edit().putString("DeviceID",androidId).apply();
        return androidId;

    }

    void switchPager(){
        Intent i = new Intent(MainActivity.this, ListeActivity.class);
        MainActivity.this.startActivity(i);

    }

}
