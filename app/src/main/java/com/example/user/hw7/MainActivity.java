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
    SearchView searchView;
    SQLiteDatabase dbrw;
    MapFragment mapFragment;
    String CurrentFun;

    class Data{
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

        mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.parkmap);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

            }
        });


        MyDBHelper dbhelper = new MyDBHelper(this);
        dbrw = dbhelper.getWritableDatabase();
        dbrw.execSQL("DELETE FROM ParkTable");

        searchView = (SearchView)findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("輸入停車場名稱");
        searchView.setOnQueryTextListener(SearchClick);

        actionMenu = (FloatingActionMenu) findViewById(R.id.action_menu);
        action_all= (com.github.clans.fab.FloatingActionButton) findViewById(R.id.action_all);
        action_star = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.action_star);
        action_car = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.action_car);
        action_moto = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.action_moto);
        action_bike = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.action_bike);
        action_all.setOnClickListener(ActionAll);
        action_star.setOnClickListener(ActionStar);
        action_car.setOnClickListener(ActionCar);
        action_moto.setOnClickListener(ActionMoto);
        action_bike.setOnClickListener(ActionBike);

        InitLoad();
        MapInitLocate();
    }

    private void RegLoad(String name, Boolean star) {
        ContentValues cv= new ContentValues();
        if (star == true)
            cv.put("star","True");
        else
            cv.put("star","False");
        dbrw.update("ParkTable",cv,"name="+"'"+name+"'",null);
    }


    private void InitLoad() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(
                "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=a880adf3-d574-430a-8e29-3192a41897a5"
        ).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                Intent i = new Intent("MyMessage");
                i.putExtra("json", response.body().string());
                sendBroadcast(i);
            }
        });

        BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String myJson = intent.getExtras().getString("json");
                Gson gson = new Gson();
                Data data = gson.fromJson(myJson, Data.class);

                Map jsonObject = (Map) gson.fromJson(myJson, Object.class);
                Map result = (Map) jsonObject.get("result");
                List results = (List) result.get("results");
                for (int i = 0; i < results.size(); i++) {
                    LinkedTreeMap<String, String> value = (LinkedTreeMap<String, String>) results.get(i);
                    ContentValues cv = new ContentValues();
                    cv.put("name", value.get("停車場名稱"));
                    cv.put("longitude", value.get("經度(WGS84)"));
                    cv.put("latitude", value.get("緯度(WGS84)"));

                    String[] colum={"name"};
                    Cursor c;
                    c = dbrw.query("Regularly",colum,"name="+"'"+value.get("停車場名稱")+"'",null,null,null,null);
                    if(c.getCount()!= 0)
                        cv.put("star", "True");
                    else
                        cv.put("star", "False");

                    if(value.get("停車場名稱").indexOf("自行車")>-1){
                        cv.put("type", "Bike");
                    }
                    else if (value.get("停車場名稱").indexOf("機車")>-1) {
                        cv.put("type", "Moto");
                    }
                    else{
                        cv.put("type", "Car");
                    }
                    dbrw.insert("ParkTable", null, cv);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter("MyMessage");
        registerReceiver(myBroadcastReceiver, intentFilter);
    }



    private void MapMarker(final String name, final String longitude, final String latitude, final String type, final String star) {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if(ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                        ActivityCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                    return;
                }
                //googleMap.setMyLocationEnabled(true);
                MarkerOptions ml = new MarkerOptions();
                if (star.equals("True"))
                    ml.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_star));
                else
                    switch(type){
                        case "Car":
                            ml.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_car));
                            break;
                        case "Moto":
                            ml.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_moto));
                            break;
                        case "Bike":
                            ml.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_bike));
                            break;
                        default:
                            break;
                    }
                ml.position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)));
                ml.title(name);

                CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(this);
                googleMap.setInfoWindowAdapter(customInfoWindowAdapter);
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        MapInfoClick(marker);
                    }
                });
                googleMap.addMarker(ml);
            }
        });
    }

    private void MapInfoClick(final Marker marker) {
        final String[] item = {"經常使用","導航"};
        AlertDialog.Builder list = new AlertDialog.Builder(MainActivity.this);
        list.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case 0:
                        String[] colum={"name"};
                        Cursor c;
                        c = dbrw.query("Regularly",colum,"name="+"'"+marker.getTitle()+"'",null,null,null,null);
                        if (c.getCount()>0){
                            dbrw.delete("Regularly","name="+"'"+marker.getTitle()+"'",null);
                            Toast.makeText(getApplicationContext(),"移除經常使用 "+marker.getTitle(),Toast.LENGTH_SHORT).show();
                            RegLoad(marker.getTitle(),false);
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"加入經常使用 "+marker.getTitle(),Toast.LENGTH_SHORT).show();
                            ContentValues cv= new ContentValues();
                            cv.put("name",marker.getTitle());
                            dbrw.insert("Regularly",null,cv);
                            RegLoad(marker.getTitle(),true);
                        }
                        RefreshMap();
                        break;
                    case 1:
                        break;
                }
            }
        });
        list.show();
    }

    private void RefreshMap() {
        switch (CurrentFun){
            case "All":
                action_all.performClick();
                break;
            case "Star":
                action_star.performClick();
                break;
            case "Car":
                action_car.performClick();
                break;
            case "Moto":
                action_moto.performClick();
                break;
            case "Bike":
                action_bike.performClick();
                break;
        }
    }

    private SearchView.OnQueryTextListener SearchClick = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private Button.OnClickListener ActionBike = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            CurrentFun = "Bike";
            //Toast.makeText(getApplicationContext(),"Bike",Toast.LENGTH_SHORT).show();
            MapInitLocate();
            String[] colum={"name","longitude","latitude","type","star"};
            Cursor c;
            c = dbrw.query("ParkTable",colum,"type ='Bike'",null,null,null,null);
            if(c.getCount()>0){
                c.moveToFirst();
                for(int i=0;i<c.getCount();i++){
                    MapMarker(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                    c.moveToNext();
                }
            }
        }
    };

    private Button.OnClickListener ActionMoto = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            CurrentFun = "Moto";
            //Toast.makeText(getApplicationContext(),"Moto",Toast.LENGTH_SHORT).show();
            MapInitLocate();
            String[] colum={"name","longitude","latitude","type","star"};
            Cursor c;
            c = dbrw.query("ParkTable",colum,"type ='Moto'",null,null,null,null);
            if(c.getCount()>0){
                c.moveToFirst();
                for(int i=0;i<c.getCount();i++){
                    MapMarker(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                    c.moveToNext();
                }
            }
        }
    };

    private Button.OnClickListener ActionCar = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            CurrentFun = "Car";
            //Toast.makeText(getApplicationContext(),"Car",Toast.LENGTH_SHORT).show();
            MapInitLocate();
            String[] colum={"name","longitude","latitude","type","star"};
            Cursor c;
            c = dbrw.query("ParkTable",colum,"type ='Car'",null,null,null,null);
            if(c.getCount()>0){
                c.moveToFirst();
                for(int i=0;i<c.getCount();i++){
                    MapMarker(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                    c.moveToNext();
                }
            }
        }
    };

    private Button.OnClickListener ActionStar = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            CurrentFun = "Star";
            //Toast.makeText(getApplicationContext(),"Regularly",Toast.LENGTH_SHORT).show();
            MapInitLocate();
            String[] colum={"name","longitude","latitude","type","star"};
            Cursor c;
            c = dbrw.query("ParkTable",colum,"star ='True'",null,null,null,null);
            if(c.getCount()>0){
                c.moveToFirst();
                for(int i=0;i<c.getCount();i++){
                    MapMarker(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                    c.moveToNext();
                }
            }
        }
    };

    private Button.OnClickListener ActionAll = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            CurrentFun = "All";
            //Toast.makeText(getApplicationContext(),"All",Toast.LENGTH_SHORT).show();
            MapInitLocate();
            String[] colum={"name","longitude","latitude","type","star"};
            Cursor c;
            c = dbrw.query("ParkTable",colum,null,null,null,null,null);
            if(c.getCount()>0){
                c.moveToFirst();
                for(int i=0;i<c.getCount();i++){
                    MapMarker(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                    c.moveToNext();
                }
            }
        }
    };

    private void MapInitLocate() {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.clear();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.043767,121.533923),11));
            }
        });
    }

    public class CustomInfoWindowAdapter implements  GoogleMap.InfoWindowAdapter{

        public CustomInfoWindowAdapter(OnMapReadyCallback context){

        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(final Marker marker) {
            View view = getLayoutInflater().inflate(R.layout.mapinfo,null);
            TextView title = (TextView)view.findViewById(R.id.mapinfo_title);
            title.setText(marker.getTitle());
            return view;
        }
    }
}
