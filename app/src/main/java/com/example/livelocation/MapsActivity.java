package com.example.livelocation;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String user;
    private Timer t;
    private Handler h;
    LatLng prev=null;
    Marker prevmarker=null;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        user = getIntent().getStringExtra("user");
        h = new Handler(Looper.getMainLooper());

        pd = new ProgressDialog(MapsActivity.this);
        pd.setCancelable(false);
        pd.setMessage("Map Loading");
        pd.show();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        pd.dismiss();
        mMap = googleMap;

        mMap.setMinZoomPreference(18.0f);

        t=new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("MapTimer", "Map Updating");
                getUpdateLocation();
            }
        },0,1000);
    }

    private void getUpdateLocation() {
        OkHttpClient client=new OkHttpClient();
        Request r=new Request.Builder()
                .url("https://frozen-crag-85320.herokuapp.com/see/"+user)
                .get()
                .build();

        client.newCall(r).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("httperr", e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String body = response.body().string();
                Log.d("locationbody", body);

                try {
                    JSONObject obj = new JSONObject(body);
                    String lat,log;
                    lat=obj.getString("lat");
                    log = obj.getString("lag");
                    Log.d("location", lat + "--------------" + log);

                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            LatLng loc = new LatLng(Double.parseDouble(lat),Double.parseDouble(log));


                            MarkerOptions m=new MarkerOptions().position(loc).title(user).icon(BitmapFromVector(MapsActivity.this,R.drawable.ic_rec));
                            if (prev != null ) {
                            mMap.addPolyline(new PolylineOptions().add(prev,loc));
                            }
                            if (prevmarker != null) {
                                prevmarker.remove();
                            }
                            prevmarker=mMap.addMarker(m);
                            prev=loc;

                            mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
                        }
                        private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
                            // below line is use to generate a drawable.
                            Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

                            // below line is use to set bounds to our vector drawable.
                            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

                            // below line is use to create a bitmap for our
                            // drawable which we have added.
                            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

                            // below line is use to add bitmap in our canvas.
                            Canvas canvas = new Canvas(bitmap);

                            // below line is use to draw our
                            // vector drawable in canvas.
                            vectorDrawable.draw(canvas);

                            // after generating our bitmap we are returning our bitmap.
                            return BitmapDescriptorFactory.fromBitmap(bitmap);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("jsonerror", e.getMessage());
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            t.cancel();
                            Toast.makeText(MapsActivity.this, "No Live Location Found", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        t.cancel();
    }
}