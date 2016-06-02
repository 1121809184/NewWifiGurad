package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.adapter.PoliceWifiAdapter;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.listener.WifiObserver;
import com.sharedream.wifiguard.listener.WifiSubject;
import com.sharedream.wifiguard.manager.WukongWifiManager;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PoliceActivity extends BaseActivity implements WifiObserver, AdapterView.OnItemClickListener {
    private ListView lvPoliceWifi;
    private Button btnPoliceCall;

    private List<ScanResult> listWifi;
    private PoliceWifiAdapter policeWifiAdapter;
    private LinkedList<ScanResult> indexList = new LinkedList<ScanResult>();
    private HashMap<Integer,Integer> selectState;
    private boolean isRefresh = false;
    private ProgressDialog progressDialog;


    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, PoliceActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WukongWifiManager.getInstance().scanWifi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiSubject.getInstance().unregistObserver(this);
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        WifiSubject.getInstance().registObserver(this);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        lvPoliceWifi = ((ListView) findViewById(R.id.lv_police_wifi));
        btnPoliceCall = ((Button) findViewById(R.id.btn_police_call));

        progressDialog = new ProgressDialog(this,ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("正在扫描WiFi，请稍后.....");
        progressDialog.show();
    }

    private void initData() {
        selectState = new HashMap<Integer,Integer>();

    }

    private void setListener() {
        lvPoliceWifi.setOnItemClickListener(this);
        btnPoliceCall.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_police_call:
                callPolice();
                break;
        }
    }

    private void callPolice() {
        for (int i = 0; i < indexList.size(); i++) {
            ScanResult scanResult = indexList.get(i);
            LogUtils.d(scanResult.SSID);
        }
        MyUtils.showToast("上报成功，风险消除前请勿使用该WiFi",this);

        clearMark();
    }

    private void clearMark(){
        indexList.clear();
        for(int i = 0; i < listWifi.size(); i++){
            selectState.put(i,0);
        }
        policeWifiAdapter.setSelectState(selectState);
        policeWifiAdapter.notifyDataSetChanged();
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_police;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_police);
        return title;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScanResult scanResult = listWifi.get(position);
        if (indexList.contains(scanResult)) {
            indexList.remove(scanResult);
            selectState.put(position,0);
        } else {
            indexList.add(scanResult);
            selectState.put(position, 1);
        }
        policeWifiAdapter.setSelectState(selectState);
        policeWifiAdapter.notifyDataSetChanged();
    }

    private void getWifiData(List<ScanResult> listTempResult) {
        if (listWifi != null) {
            listWifi.clear();
        }
        listWifi = new ArrayList<ScanResult>();

        for (ScanResult scanResult : listTempResult) {
            if (scanResult.SSID == null || scanResult.SSID.trim().length() == 0) {
                continue;
            }
            listWifi.add(scanResult);
        }

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                ScanResult sr1 = (ScanResult) o1;
                ScanResult sr2 = (ScanResult) o2;
                if (sr1.level > sr2.level)
                    return -1;
                else if (sr1.level == sr2.level)
                    return 0;
                else if (sr1.level < sr2.level)
                    return 1;
                return 0;
            }
        };
        Collections.sort(listWifi, comp);
        for(int i = 0; i < listWifi.size(); i++){
            selectState.put(i,0);
        }
        if (policeWifiAdapter == null) {
            policeWifiAdapter = new PoliceWifiAdapter(listWifi,selectState);
            lvPoliceWifi.setAdapter(policeWifiAdapter);
        } else {
            policeWifiAdapter.setListWifi(listWifi);
            policeWifiAdapter.setSelectState(selectState);
            policeWifiAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onWifiOpen() {

    }

    @Override
    public void onWifiClose() {

    }

    @Override
    public void onWifiDisconnected() {

    }

    @Override
    public void onWifiConnecting() {

    }

    @Override
    public void onWifiPasswordVerifying() {

    }

    @Override
    public void onWifiCompleted() {

    }

    @Override
    public void onWifiPasswordIncorrect() {

    }

    @Override
    public void onWifiPasswordCorrect() {

    }

    @Override
    public void onWifiIpObtaining() {

    }

    @Override
    public void onWifiConnected() {

    }

    @Override
    public void onWifiConnectTimeout() {

    }

    @Override
    public void onWifiScanResultChanged(List<ScanResult> listResult, List<WifiConfiguration> listWifiConfiguration) {
        if (!isRefresh) {
            progressDialog.dismiss();
            getWifiData(listResult);
            isRefresh = true;
        }

    }

    @Override
    public void onAvailableWifiFound(String ssid) {

    }

    @Override
    public void onAvailableWifiNotFound() {

    }
}
