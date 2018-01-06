package com.example.user.hw7;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Camera;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FloatingActionMenu actionMenu;
    com.github.clans.fab.FloatingActionButton action_all, action_star, action_car, action_moto, action_bike;

    public int type;

     final class Data{
        Result result;
        class Result{
            Results[] results;
            class Results{
                String 停車場名稱;
                String 經度;
                String 緯度;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.github.clans.fab.FloatingActionButton action_all = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.action_all);
        com.github.clans.fab.FloatingActionButton action_car = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.action_car);
        com.github.clans.fab.FloatingActionButton action_bike = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.action_bike);
        com.github.clans.fab.FloatingActionButton action_moto = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.action_moto);
        com.github.clans.fab.FloatingActionButton action_star = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.action_star);



        action_all.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                type=0;
                GetIntent();
            }
        });
        action_bike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                type=1;
                GetIntent();
            }
        });
        action_moto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                type=2;
                GetIntent();
            }
        });
        action_car.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                type=3;
                GetIntent();
            }
        });

        BroadcastReceiver myBroadcasReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String myJson = intent.getExtras().getString("json");

                Gson gson = new Gson();
                Data data = gson.fromJson(myJson,Data.class);

                Map jsonObject = (Map)gson.fromJson(myJson,Object.class);
                Map result = (Map) jsonObject.get("result");

                final List results = (List) result.get("results");

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.parkmap);

                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            if(ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                                return;
                            }
                            googleMap.setMyLocationEnabled(true);
                            MarkerOptions m = new MarkerOptions();

                            if(type==0)
                            for( int i = 0; i < results.size(); i++) {
                                LinkedTreeMap<String, String> value = (LinkedTreeMap<String, String>) results.get(i);

                                if(i==0){
                                    googleMap.clear();
                                }

                                m.position(new LatLng(Double.parseDouble(value.get("緯度(WGS84)")),Double.parseDouble(value.get("經度(WGS84)"))));
                                m.draggable(true);
                                m.title(value.get("停車場名稱"));
                                googleMap.addMarker(m);

                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.033739,121.527886),11));
                            }
                            if(type==1)
                            for( int k = 0; k < results.size(); k++) {
                                LinkedTreeMap<String, String> value = (LinkedTreeMap<String, String>) results.get(k);

                                if(k==0){
                                    googleMap.clear();
                                }

                                if(value.get("停車場名稱").indexOf("自行車")>-1){
                                    m.position(new LatLng(Double.parseDouble(value.get("緯度(WGS84)")),Double.parseDouble(value.get("經度(WGS84)"))));
                                    m.draggable(true);
                                    m.title(value.get("停車場名稱"));
                                    googleMap.addMarker(m);
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.033739,121.527886),11));
                                }
                            }
                            if(type==2)
                            for( int o = 0; o < results.size(); o++) {
                                LinkedTreeMap<String, String> value = (LinkedTreeMap<String, String>) results.get(o);

                                if(o==0){
                                    googleMap.clear();
                                }

                                if(value.get("停車場名稱").indexOf("機車")>-1){
                                    m.position(new LatLng(Double.parseDouble(value.get("緯度(WGS84)")),Double.parseDouble(value.get("經度(WGS84)"))));
                                    m.draggable(true);
                                    m.title(value.get("停車場名稱"));
                                    googleMap.addMarker(m);
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.033739,121.527886),11));
                                }
                            }
                            if(type==3)
                            for( int u = 0; u < results.size(); u++) {
                                LinkedTreeMap<String, String> value = (LinkedTreeMap<String, String>) results.get(u);

                                if(u==0){
                                    googleMap.clear();
                                }

                                if(value.get("停車場名稱").indexOf("機車")==-1 && value.get("停車場名稱").indexOf("自行車")==-1 ){
                                    m.position(new LatLng(Double.parseDouble(value.get("緯度(WGS84)")),Double.parseDouble(value.get("經度(WGS84)"))));
                                    m.draggable(true);
                                    m.title(value.get("停車場名稱"));
                                    googleMap.addMarker(m);
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.033739,121.527886),11));
                                }
                            }

                        }
                    });

            }
        };
        IntentFilter intentFilter = new IntentFilter("MyMessage");
        registerReceiver(myBroadcasReceiver, intentFilter);
    }

    private void GetIntent(){
        OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://data.taipei/opendata/datalist/apiAccess"
                        +"?scope=resourceAquire&rid=a880adf3-d574-430a-8e29-3192a41897a5")
                .build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e) {}

            @Override
            public void onResponse(final Response response) throws IOException
            {
                Intent i = new Intent("MyMessage");
                i.putExtra("json",response.body().string());
                sendBroadcast(i);
            }
        });
    }

}
