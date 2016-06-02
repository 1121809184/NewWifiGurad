package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.adapter.SusWifiAdapter;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SuspiciousWifiActivity extends BaseActivity {
    private TextView tvShapeCount;
    private TextView tvFoundCount;
    private TextView tvReCheck;
    private ListView lvWifi;

    public static void launch(Activity activity, int count, ArrayList<ScanResult> scanResultList) {
        Intent intent = new Intent(activity, SuspiciousWifiActivity.class);
        intent.putExtra("count", count);
        intent.putParcelableArrayListExtra("scanResultList", scanResultList);
        activity.startActivityForResult(intent, RESULT_FIRST_USER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initAfterSetContentView() {
        enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        tvShapeCount = (TextView) findViewById(R.id.tv_shape_count);
        tvFoundCount = (TextView) findViewById(R.id.tv_found_count);
        tvReCheck = (TextView) findViewById(R.id.tv_recheck);
        lvWifi = (ListView) findViewById(R.id.lv_sus_wifi);
    }

    private void initData() {
        Intent intent = getIntent();
        int count = intent.getIntExtra("count", 0);
        ArrayList<ScanResult> scanResultList = intent.getParcelableArrayListExtra("scanResultList");
        LogUtils.d("count >>> " + count + ",scanR >>> " + scanResultList.size());
        Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                if (lhs.level > rhs.level) {
                    return -1;
                } else if (lhs.level == rhs.level) {
                    return 0;
                } else if (lhs.level < rhs.level) {
                    return 1;
                }
                return 0;
            }
        };
        Collections.sort(scanResultList, comparator);
        for (int i = 0; i < scanResultList.size(); i++) {
            ScanResult scanResult = scanResultList.get(i);
            if (TextUtils.isEmpty(scanResult.SSID)) {
                scanResultList.remove(scanResult);
                count--;
            }
        }

        tvShapeCount.setText(String.valueOf(count));
        String format = AppContext.getContext().getResources().getString(R.string.activity_sus_wifi_count);
        SpannableString styleText = new SpannableString(String.format(format, count));
        styleText.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BlueFontStyle), 5, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvFoundCount.setText(styleText);

        SusWifiAdapter susWifiAdapter = new SusWifiAdapter(scanResultList);
        lvWifi.setAdapter(susWifiAdapter);
    }

    private void setListener() {
        tvReCheck.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.tv_recheck:
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_suspicious_wifi;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_scan_wifi);
        return title;
    }
}
