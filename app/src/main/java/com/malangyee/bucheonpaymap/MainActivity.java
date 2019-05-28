package com.malangyee.bucheonpaymap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    public DBManager dbManager;

    ListView listView;
    List<Marker> markerList = new ArrayList<>();
    Marker selectedMarker;

    String[] permission_list = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};

    boolean locationTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        dbManager = new DBManager(this);
        dbManager.open();
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        locationTag = true;

        listView = findViewById(R.id.lv);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            }
        });

        MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.frag_map);
        if(mapFragment == null){
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.frag_map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);



    }

    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {

        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        naverMap.setOnMapLongClickListener(new NaverMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull PointF pointF, @NonNull final LatLng latLng) {
                for(Marker marker : markerList){
                    marker.setMap(null);
                }

                setUI(latLng, naverMap);

            }
        });

        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull android.location.Location location) {
                if(locationTag){
                    CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(location.getLatitude(), location.getLongitude()));
                    naverMap.moveCamera(cameraUpdate);

                    setUI(new LatLng(location.getLatitude(), location.getLongitude()), naverMap);

                    locationTag = false;
                }
            }
        });
    }

    public void setUI(LatLng latLng, final NaverMap naverMap){

        final List<Location> locationList = dbManager.getNearByLocation(latLng.latitude, latLng.longitude, 0.3);
        Log.d(getClass().getName(), "################# ("+latLng.latitude+", "+latLng.longitude+")");



        List<String> locationName = new ArrayList<>();
        markerList = new ArrayList<>();

        for(Location loc : locationList){
            locationName.add(loc.getName());

            Marker marker = new Marker();
            marker.setPosition(new LatLng(loc.getLat(), loc.getLng()));

            for(Marker m : markerList){
                if(m.getPosition().equals(marker.getPosition()))
                    marker.setCaptionOffset(marker.getCaptionOffset()+30);
            }
            marker.setCaptionText(loc.getName());
            marker.setHideCollidedSymbols(true);
            marker.setMap(naverMap);
            markerList.add(marker);
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, locationName);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedMarker != null){
                    selectedMarker.setIcon(Marker.DEFAULT_ICON);
                    selectedMarker.setZIndex(Marker.DEFAULT_GLOBAL_Z_INDEX);
                }
                selectedMarker = markerList.get(position);
                selectedMarker.setZIndex(100);
                selectedMarker.setIcon(MarkerIcons.BLUE);
                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(locationList.get(position).getLat(), locationList.get(position).getLng()));
                naverMap.moveCamera(cameraUpdate);
            }
        });
    }

    public void checkPermission(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        for(String permission : permission_list){
            int chk = checkCallingOrSelfPermission(permission);

            if(chk == PackageManager.PERMISSION_DENIED){
                requestPermissions(permission_list,0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0){
            for(int i=0; i<grantResults.length; i++){
                if(grantResults[i] ==PackageManager.PERMISSION_GRANTED){
                }else{
                    Toast.makeText(getApplicationContext(), "어플리케이션을 이용하기 위해서는 권한 설정이 필요합니다.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
        if(locationSource.onRequestPermissionsResult(requestCode, permissions,grantResults)){
            finish();
        }
    }
}
