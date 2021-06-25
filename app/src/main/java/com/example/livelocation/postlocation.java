package com.example.livelocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.*;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class postlocation extends Service {
    private String CHANNEL_ID = "LiveLocation";
    private int NID = 32432434;
    NotificationCompat.Builder builder;

    boolean run = true;


    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    LocationSettingsRequest locationSettingsRequest;


    long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;


    String user;

    public postlocation() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        user = intent.getStringExtra("user");
        createNotificationChannel();
        Intent i = new Intent(getApplicationContext(), MyReceiver.class);
        PendingIntent p = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Live Location")
                .setContentText("Private Key : " + user)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_baseline_edit_location_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_baseline_close_24, "STOP", p);

        startForeground(NID, builder.build());


        getLocationandpost();


        return START_STICKY;
    }

    @SuppressLint("RestrictedApi")
    private void getLocationandpost() {




        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder1 = new LocationSettingsRequest.Builder();
        builder1.addLocationRequest(locationRequest);
        locationSettingsRequest = builder1.build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult); // why? this. is. retarded. Android.
                Location currentLocation = locationResult.getLastLocation();

                Log.d("location", "Location Callback results: " + currentLocation.getLatitude() + "----------------" + currentLocation.getLongitude());
                //todo : send request
                sendPost(currentLocation);
                //builder.setContentText("Latitude : " + currentLocation.getLatitude() + "\n" + "Longtitude : " + currentLocation.getLongitude());
                startForeground(NID, builder.build());
            }
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.myLooper());
    }

    private void sendPost(Location currentLocation) {
        OkHttpClient client = new OkHttpClient();
        Request r=new Request.Builder()
                .url("https://frozen-crag-85320.herokuapp.com/live/"+user+"/"+currentLocation.getLatitude()+"/"+currentLocation.getLongitude())
                .get()
                .build();

        client.newCall(r).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {

            }
        });



    }

    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    private void turnGPSOff(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    @Override
    public void onDestroy() {
        run=false;
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "LiveLocation";
            String description = "Live Location";
            int importance = NotificationManager.IMPORTANCE_NONE;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }


}