package com.example.livelocation;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.security.Permission;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Button start;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    Timer t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);




        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d("ads", initializationStatus.toString());
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);




        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5556989253157684/5452472156");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        t=new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("ads", mInterstitialAd.isLoading()+"");
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
                        }
                    }
                });

            }
        },0,1000);



        start = findViewById(R.id.startapp);


        Dialog d=new Dialog(MainActivity.this);
        d.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
        d.setContentView(R.layout.dialog1);
        EditText user = d.findViewById(R.id.userid);
        Button submit = d.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = user.getText().toString();
                if (text != null && !text.isEmpty()) {
                    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        buildAlertMessageNoGps();

                    }
                    else if(!internetIsConnected()){
                        buildAlertMessageNointernet();
                    }
                    else{

                    Intent i=new Intent(MainActivity.this,postlocation.class);
                    i.putExtra("user",text);
                    startService(i);
                    d.dismiss();
                    Toast.makeText(MainActivity.this,"Location Sharing to private key Started",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        Dialog d2=new Dialog(MainActivity.this);
        d2.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
        d2.setContentView(R.layout.dialog1);
        EditText user2 = d2.findViewById(R.id.userid);
        Button submit2 = d2.findViewById(R.id.submit);

        submit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = user2.getText().toString();
                if (text != null && !text.isEmpty()) {
                    Intent i=new Intent(MainActivity.this,MapsActivity.class);
                    i.putExtra("user",text);
                    startActivity(i);
                    d2.dismiss();
                }
            }
        });


        Dialog d1=new Dialog(MainActivity.this);
        d1.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
        d1.setContentView(R.layout.dialoginit);
        Button share,see;
        share = d1.findViewById(R.id.share);
        see = d1.findViewById(R.id.see);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d1.dismiss();
                d.show();
            }
        });




        see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d1.dismiss();
                d2.show();
            }
        });




        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d1.show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean flag = true;
        if (requestCode == 100) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    flag = false;
                    break;
                }
            }

            if (flag == false) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);
            }
        }
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    private void buildAlertMessageNointernet() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You are not connected to Internet!!!!")
                .setCancelable(true);
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        t.cancel();
    }
}