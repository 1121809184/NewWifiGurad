package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;

public class LocationActivity extends Activity implements View.OnClickListener, OnGetGeoCoderResultListener {
    public static final int MAP_DEFAULT_ZOOM = 18;
    public static final int MAP_DEFAULT_OVER_LOOK = -10;

    private BaiduMap baiduMap;
    private MapView mapView;
    private float lat;
    private float lng;
    private EditText etInputAddress;
    private TextView tvUseMapAddress;
    private ImageView ivOriginalLocation;
    private ImageView ivLocate;

    private GeoCoder geoCoder;
    private String address;
    private String province;
    private String city;
    private String district;
    private String street;
    private String streetNumber;
    private double longitude;
    private double latitude;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, LocationActivity.class);
        activity.startActivityForResult(intent, Constant.SERVER_SUCCESS_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        mapView = ((MapView) findViewById(R.id.mapView));
        ivOriginalLocation = ((ImageView) findViewById(R.id.iv_original_location));
        ivLocate = ((ImageView) findViewById(R.id.iv_locate));
        etInputAddress = ((EditText) findViewById(R.id.et_input_address));
        tvUseMapAddress = ((TextView) findViewById(R.id.tv_use_map_address));

        initMapConfig();
    }

    private void initMapConfig() {
        mapView.showScaleControl(false);
        mapView.showZoomControls(false);
        baiduMap = mapView.getMap();
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setMyLocationEnabled(true);

        UiSettings uiSettings = baiduMap.getUiSettings();
        uiSettings.setCompassEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setOverlookingGesturesEnabled(false);
    }


    private void initData() {
        geoCoder = GeoCoder.newInstance();
        lng = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LNG, 0);
        lat = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LAT, 0);
        MapStatus mapStatus = new MapStatus.Builder().zoom(MAP_DEFAULT_ZOOM).overlook(MAP_DEFAULT_OVER_LOOK).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);
        updateMapLocation();

    }

    private void updateMapLocation() {
        MyLocationData mLocationData = new MyLocationData.Builder().direction(100).latitude(lat).longitude(lng).build();
        baiduMap.setMyLocationData(mLocationData);
        LatLng latlng = new LatLng(lat, lng);
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latlng);
        baiduMap.setMapStatus(mapStatusUpdate);

        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latlng));
    }

    private void setListener() {
        ivOriginalLocation.setOnClickListener(this);
        geoCoder.setOnGetGeoCodeResultListener(this);
        tvUseMapAddress.setOnClickListener(this);

        baiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            LatLng start, finish;

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                start = mapStatus.target;
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                finish = mapStatus.target;
                if (start.latitude != finish.latitude || start.longitude != finish.longitude) {
                    Projection projection = baiduMap.getProjection();
                    Point startPoint = projection.toScreenLocation(start);
                    Point finishPoint = projection.toScreenLocation(finish);
                    int dx = Math.abs(finishPoint.x - startPoint.x);
                    int dy = Math.abs(finishPoint.y - startPoint.y);
                    if (dx > 0 || dy > 0) {
                        int x = mapView.getMeasuredWidth() / 2;
                        int y = mapView.getMeasuredHeight() / 2;
                        if (baiduMap != null) {
                            Point point = new Point(x, y);
                            LatLng latLng = projection.fromScreenLocation(point);
                            ivLocate.startAnimation(AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.location_mark_animation));
                            geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_original_location:
                afreshLocation();
                break;
            case R.id.tv_use_map_address:
                finishAndSetResult();
                break;
        }
    }

    private void finishAndSetResult() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.BUNDLE_KEY_LOC_PROVINCE, province);
        bundle.putString(Constant.BUNDLE_KEY_LOC_CITY, city);
        bundle.putString(Constant.BUNDLE_KEY_LOC_DISTRICT, district);
        bundle.putString(Constant.BUNDLE_KEY_LOC_STREET, street);
        bundle.putString(Constant.BUNDLE_KEY_LOC_STREET_NUMBER, streetNumber);
        bundle.putDouble(Constant.BUNDLE_KEY_LNG,longitude);
        bundle.putDouble(Constant.BUNDLE_KEY_LAT,latitude);
        intent.putExtras(bundle);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }

    private void afreshLocation() {
        LatLng latlng = new LatLng(lat, lng);
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latlng);
        baiduMap.animateMapStatus(mapStatusUpdate);
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(LocationActivity.this, "定位出错", Toast.LENGTH_LONG).show();
            return;
        }
        address = reverseGeoCodeResult.getAddress();
        province = reverseGeoCodeResult.getAddressDetail().province;
        city = reverseGeoCodeResult.getAddressDetail().city;
        district = reverseGeoCodeResult.getAddressDetail().district;
        street = reverseGeoCodeResult.getAddressDetail().street;
        streetNumber = reverseGeoCodeResult.getAddressDetail().streetNumber;
        longitude = reverseGeoCodeResult.getLocation().longitude;
        latitude = reverseGeoCodeResult.getLocation().latitude;

        etInputAddress.setText(address);
    }
}
