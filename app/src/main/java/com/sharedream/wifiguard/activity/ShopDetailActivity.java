package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.adapter.CategoryListAdapter;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdCreateToken;
import com.sharedream.wifiguard.cmdws.CmdGetMyShop;
import com.sharedream.wifiguard.cmdws.CmdShopCategory;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.dialog.CategoryDialog;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.utils.RealPathUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopDetailActivity extends BaseActivity {
    private ImageView ivManagerAddShopLogo;
    private EditText etShopName;
    private EditText etShopPhone;
    private EditText etShopPerson;
    private EditText etShopAddress;
    private TextView etShopCategoryBig;
    private TextView etShopCategorySmall;
    private Button btnShopSave;
    private RelativeLayout rlShopCategorySmall;
    private RelativeLayout rlShopCategoryBig;
    private TextView tvUseAddress;
    private ProgressDialog progressDialog;

    private CategoryListAdapter categoryListAdapter;
    private com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop myShop;
    private List<CmdShopCategory.BigCategory> bigCategoryList;
    private List<CmdShopCategory.SmallCategory> smallCategoryList;

    private boolean saveToSQLite;
    private ImageView ivAddressStar;
    private ImageView ivShopNameStar;
    private double lng;
    private double lat;
    private UploadManager uploadManager = new UploadManager();
    private String uploadFilePath;
    private ProgressDialog pd;
    private String logoSrc;
    private String uptoken;

    private int category;
    private String cityId;
    private String shopAddr;
    private String shopName;
    private String shopPhone;
    private String shopPerson;
    private int shopId;

    public static void launch(Activity activity, CmdGetMyShop.Shop myShop) {
        Intent intent = new Intent(activity, ShopDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constant.BUNDLE_KEY_MY_SHOP, myShop);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, RESULT_FIRST_USER);
    }

    @Override
    protected void initAfterSetContentView() {
        enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        ivManagerAddShopLogo = ((ImageView) findViewById(R.id.iv_manager_add_shop_logo));
        etShopName = ((EditText) findViewById(R.id.et_shop_name));
        etShopPhone = ((EditText) findViewById(R.id.et_shop_phone));
        etShopPerson = ((EditText) findViewById(R.id.et_shop_person));
        etShopAddress = ((EditText) findViewById(R.id.et_shop_address));
        tvUseAddress = ((TextView) findViewById(R.id.tv_use_address));
        etShopCategoryBig = ((TextView) findViewById(R.id.et_shop_category_big));
        etShopCategorySmall = ((TextView) findViewById(R.id.et_shop_category_small));
        rlShopCategorySmall = ((RelativeLayout) findViewById(R.id.rl_shop_category_small));
        rlShopCategoryBig = ((RelativeLayout) findViewById(R.id.rl_shop_category_big));
        btnShopSave = ((Button) findViewById(R.id.btn_shop_save));
        ivShopNameStar = ((ImageView) findViewById(R.id.iv_shop_name_star));
        ivAddressStar = ((ImageView) findViewById(R.id.iv_address_star));

        addTextWatcher();
    }

    private void addTextWatcher() {
        etShopName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".equals(s.toString().trim())) {
                    ivShopNameStar.setVisibility(View.VISIBLE);
                } else {
                    ivShopNameStar.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etShopAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".equals(s.toString().trim())) {
                    ivAddressStar.setVisibility(View.VISIBLE);
                } else {
                    ivAddressStar.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        myShop = bundle.getParcelable(Constant.BUNDLE_KEY_MY_SHOP);
        if (myShop == null) {
            return;
        }
        lng = myShop.lng;
        lat = myShop.lat;
        shopId = myShop.shopId;
        logoSrc = myShop.logoSrc;
        etShopName.setText(myShop.name);
        etShopPhone.setText(myShop.phone1);
        etShopAddress.setText(myShop.address);
        etShopPerson.setText(myShop.owner);

//        ImageRequest imageRequest = new ImageRequest(logoSrc, new Response.Listener<Bitmap>() {
//            @Override
//            public void onResponse(Bitmap response) {
//                //给imageView设置图片
//                ivManagerAddShopLogo.setImageBitmap(response);
//            }
//        }, 100, 100, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                //设置一张错误的图片，临时用ic_launcher代替
//                ivManagerAddShopLogo.setImageResource(R.drawable.shop_sys_logo);
//            }
//        });
//        MyCmdUtil.getRequestQueue().add(imageRequest);

        MyCmdUtil.getImageLoader().get(logoSrc, ImageLoader.getImageListener(ivManagerAddShopLogo,
                R.drawable.shop_sys_logo, R.drawable.shop_sys_logo), 100, 100, ImageView.ScaleType.FIT_XY);

        saveToSQLite = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.SP_KEY_SAVE_TO_DB, false);
        if (saveToSQLite) {
            getShopCategoryFromSQLite();
            initShopCategory();
        } else {
            //getShopCategoryFromServer();
            getShopCategoryFromServerUseNewInterface();
        }
    }

    private void getShopCategoryFromServerUseNewInterface() {
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = com.sharedream.wifiguard.cmdws.CmdShopCategory.createRequestJson(accessToken);
        LogUtils.d("get shop category request >> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_SHOP_CATEGORY, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("get shop category response >> " + responseResult);
                    handleGetShopCategoryFromServerUseNewInterfaceResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("get shop category error >> " + exception.getMessage());
            }
        });
    }

    private void handleGetShopCategoryFromServerUseNewInterfaceResults(String response) {
        com.sharedream.wifiguard.cmdws.CmdShopCategory.Results results = com.sharedream.wifiguard.cmdws.CmdShopCategory.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            bigCategoryList = results.data;
            if (bigCategoryList != null) {
                com.sharedream.wifiguard.cmdws.CmdShopCategory.BigCategory bigCategory = bigCategoryList.get(0);
                etShopCategoryBig.setText(bigCategory.name);
                smallCategoryList = bigCategoryList.get(0).children;
                if (smallCategoryList != null) {
                    categoryListAdapter = new CategoryListAdapter(null, smallCategoryList);
                    com.sharedream.wifiguard.cmdws.CmdShopCategory.SmallCategory smallCategory = smallCategoryList.get(0);
                    etShopCategorySmall.setText(smallCategory.name);
                    LogUtils.d("small scene >>>>> " + smallCategory.name + ":" + smallCategory.id);
                    GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, smallCategory.id);
                }
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(100);
                    saveShopCategoryToSQLite();
                    GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SAVE_TO_DB, true);
                    SystemClock.sleep(100);

                    int categoryId = myShop.category;
                    final String[] category = DatabaseManager.findSceneNameById(categoryId);
                    if (category != null && category.length == 4) {
                        LogUtils.d("find scene >>>>> " + category[0] + ":" + category[1] + "," + category[2] + ":" + category[3]);
                        smallCategoryList = DatabaseManager.queryAllSmallScene(Integer.parseInt(category[2]));
                        GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, categoryId);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                etShopCategorySmall.setText(category[1]);
                                etShopCategoryBig.setText(category[3]);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    /*private void getShopCategoryFromServer() {
        LogUtils.d("从服务器获取分类信息");
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("正在加载，请稍后.....");
        progressDialog.show();

        String accessKey = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_KEY, null);
        String uid = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_USER_ID, null);
        String json = CmdShopCategory.createRequestJson(uid, accessKey);
        LogUtils.d("分类信息request >>>>> " + json);
        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_MERCHANT_CATEGORY, json, new BaseCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                progressDialog.dismiss();
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("分类信息response >>>>> " + responseResult);
                    handleShopCategoryResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Exception exception) {
                exception.printStackTrace();
                progressDialog.dismiss();
            }
        });
    }*/

    /*private void handleShopCategoryResults(String response) {
        CmdShopCategory.Results results = CmdShopCategory.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            bigCategoryList = results.data;
            if (bigCategoryList != null) {
                CmdShopCategory.BigCategory bigCategory = bigCategoryList.get(0);
                smallCategoryList = bigCategoryList.get(0).children;
                if (smallCategoryList != null) {
                    CmdShopCategory.SmallCategory smallCategory = smallCategoryList.get(0);
                    LogUtils.d("最小分类 >>>>> " + smallCategory.name + ":" + smallCategory.id);
                    GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, smallCategory.id);
                }
            }

            //将分类信息保存至数据库
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(100);
                    saveShopCategoryToSQLite();
                    GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SAVE_TO_DB, true);
                    LogUtils.d("将分类数据保存到数据库");
                    SystemClock.sleep(100);

                    int categoryId = myShop.category;
                    final String[] category = DatabaseManager.findSceneNameById(categoryId);
                    if (category != null && category.length == 4) {
                        LogUtils.d("查询分类 >>>>> " + category[0] + ":" + category[1] + "," + category[2] + ":" + category[3]);
                        smallCategoryList = DatabaseManager.queryAllSmallScene(Integer.parseInt(category[2]));
                        GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, categoryId);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                etShopCategorySmall.setText(category[1]);
                                etShopCategoryBig.setText(category[3]);
                            }
                        });
                    }
                }
            }).start();
        }
    }*/

    private void saveShopCategoryToSQLite() {
        if (bigCategoryList != null) {
            for (int i = 0; i < bigCategoryList.size(); i++) {
                CmdShopCategory.BigCategory bigCategory = bigCategoryList.get(i);
                DatabaseManager.insertBigScene(bigCategory.id, bigCategory.name);
                List<CmdShopCategory.SmallCategory> smallCategoryList = bigCategory.children;
                for (int j = 0; j < smallCategoryList.size(); j++) {
                    CmdShopCategory.SmallCategory smallCategory = smallCategoryList.get(j);
                    DatabaseManager.insertSmallScene(smallCategory.id, smallCategory.name, bigCategory.id);
                }
            }
        }
    }

    private void getShopCategoryFromSQLite() {
        bigCategoryList = DatabaseManager.queryAllBigScene();
        if (bigCategoryList != null) {
            CmdShopCategory.BigCategory bigCategory = bigCategoryList.get(0);
            smallCategoryList = DatabaseManager.queryAllSmallScene(bigCategory.id);
            if (smallCategoryList != null) {
                CmdShopCategory.SmallCategory smallCategory = smallCategoryList.get(0);
                LogUtils.d("small scene >>>>> " + smallCategory.name + ":" + smallCategory.id);
                GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, smallCategory.id);
            }
        }
    }

    private void initShopCategory() {
        int categoryId = myShop.category;
        String[] category = DatabaseManager.findSceneNameById(categoryId);
        if (category != null && category.length == 4) {
            LogUtils.d("find scene >>>>> " + category[0] + ":" + category[1] + "," + category[2] + ":" + category[3]);
            smallCategoryList = DatabaseManager.queryAllSmallScene(Integer.parseInt(category[2]));
            etShopCategorySmall.setText(category[1]);
            etShopCategoryBig.setText(category[3]);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, categoryId);
        }
    }

    private void setListener() {
        ivManagerAddShopLogo.setOnClickListener(this);
        tvUseAddress.setOnClickListener(this);
        rlShopCategorySmall.setOnClickListener(this);
        rlShopCategoryBig.setOnClickListener(this);
        btnShopSave.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.tv_use_address:
                startLocationActivity();
                break;
            case R.id.rl_shop_category_big:
                showBigCategoryDialog();
                break;
            case R.id.rl_shop_category_small:
                showSmallCategoryDialog();
                break;
            case R.id.btn_shop_save:
                goToBelongsActivity();
                break;
            case R.id.iv_manager_add_shop_logo:
                selectImageAndUpload();
                break;
        }
    }

    private void goToBelongsActivity() {
        category = GlobalField.restoreFieldInt(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, 0);
        cityId = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_CITY_ID, null);
        shopAddr = etShopAddress.getText().toString().trim();
        shopName = etShopName.getText().toString().trim();
        shopPhone = etShopPhone.getText().toString().trim();
        shopPerson = etShopPerson.getText().toString().trim();

        boolean isValid = checkInput(shopName, shopPhone, shopPerson, shopAddr);
        if (!isValid) {
            return;
        }
        if (uploadFilePath != null) {
            getTokenFromServer();
        } else {
            GroupBelongsActivity.launch(ShopDetailActivity.this, shopName, shopAddr, lng, lat, shopPhone, category, cityId, shopPerson, logoSrc, Constant.BUNDLE_KEY_EDIT_SHOP, shopId);
        }
        //updateShop();
    }

    private void getTokenFromServer() {
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        CmdCreateToken.Params params = CmdCreateToken.createParams(accessToken);
        String json = MyCmdUtil.convertObject2Json(params);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_CREATE_TOKEN, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                LogUtils.d("group belong response >>> " + responseResult);
                CmdCreateToken.Results result = MyCmdUtil.convertJson2Object(responseResult, CmdCreateToken.Results.class);
                handleGetToeknResults(result);
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("group belong exception >>> " + exception.getMessage());
            }
        });
    }

    private void handleGetToeknResults(CmdCreateToken.Results result) {
        if (result == null) {
            MyUtils.showToast(getString(R.string.activity_toast_info_parse_data_error), getApplicationContext());
            return;
        }

        if (result.code == Constant.SERVER_SUCCESS_CODE) {
            CmdCreateToken.Data data = result.data;
            uptoken = data.uptoken;
            String domain = data.domain;
            String name = data.name;
            uploadLogo(uptoken, domain, name);
        } else {
            MyUtils.showToast(result.msg, getApplicationContext());
        }
    }

    private void uploadLogo(String uptoken, String domain, String name) {
        if (this.uploadFilePath == null) {
            return;
        }
        try {
//            JSONObject json = new JSONObject();
//            long _dataline = System.currentTimeMillis() / 1000 + 3600;
//            json.put("deadline", _dataline);
//            json.put("scope", "wifiguard");
//            String _encodedPutPolicy = UrlSafeBase64.encodeToString(json
//                    .toString().getBytes());
//            byte[] _sign = HmacSHA1Encrypt(_encodedPutPolicy, secretKey);
//            String _encodedSign = UrlSafeBase64.encodeToString(_sign);
//            String _uploadToken = accessKey + ':' + _encodedSign + ':'
//                    + _encodedPutPolicy;

            upload(uptoken, name, domain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void upload(String uploadToken, String key, final String domain) {
        if (this.uploadManager == null) {
            this.uploadManager = new UploadManager();
        }
        pd = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在保存图片，请稍后.....");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        File uploadFile = new File(this.uploadFilePath);
        UploadOptions uploadOptions = new UploadOptions(null, null, false,
                new UpProgressHandler() {
                    @Override
                    public void progress(String key, double percent) {
                        pd.setProgress((int) (percent * 100));
                    }
                }, null);

        this.uploadManager.put(uploadFile, key, uploadToken,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo respInfo,
                                         JSONObject jsonData) {
                        pd.dismiss();
                        LogUtils.i("错误信息:" + respInfo);
                        if (respInfo.isOK()) {
                            try {
                                String fileKey = jsonData.getString("key");
                                String fileHash = jsonData.getString("hash");
                                LogUtils.i("主要信息:" + domain + ";" + key);
                                logoSrc = "http://" + domain + "/" + key + "?imageMogr2/quality/75";
                                GroupBelongsActivity.launch(ShopDetailActivity.this, shopName, shopAddr, lng, lat, shopPhone, category, cityId, shopPerson, logoSrc, Constant.BUNDLE_KEY_EDIT_SHOP, shopId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            MyUtils.showToast("图片保存失败", Toast.LENGTH_SHORT, ShopDetailActivity.this);
                        }
                    }

                }, uploadOptions);
    }

    private void selectImageAndUpload() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, Constant.RESULT_CODE_PICK_IMAGE);
    }

    private boolean checkInput(String shopName, String phone, String owner, String address) {
        if (TextUtils.isEmpty(shopName)) {
            MyUtils.showToast(AppContext.getContext().getResources().getString(R.string.activity_add_shop_name_null), Toast.LENGTH_SHORT, this);
            return false;
        } else {
            Pattern pa = Pattern.compile("!|！|\\?|？|@|◎|#|＃|￥|%|％|……|※|×|_|——|＋|§", Pattern.CASE_INSENSITIVE);
            Matcher ma = pa.matcher(shopName);
            if (ma.find() || owner.contains(" ")) {
                try {
                    MyUtils.showToast("商铺名称不能包含非法字符", Toast.LENGTH_SHORT, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        }

//        if (!TextUtils.isEmpty(phone)) {
//            Pattern p = Pattern.compile("^[1]+[3,5,8,4]+\\d{9}");
//            Matcher m = p.matcher(phone);
//            if (!m.matches()) {
//                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_err);
//                MyUtils.showToast(str, Toast.LENGTH_SHORT, this);
//                return false;
//            }
//        }

        if (!TextUtils.isEmpty(owner)) {
            Pattern pa = Pattern.compile("!|！|\\?|？|@|◎|#|＃|(\\$)|￥|%|％|(\\^)|……|(\\&)|※|(\\*)|×|(\\()|（|(\\))|）|_|——|(\\+)|＋|(\\|)|§", Pattern.CASE_INSENSITIVE);
            Matcher ma = pa.matcher(owner);
            if (ma.find() || owner.contains(" ")) {
                MyUtils.showToast("联系人姓名不能包含非法字符", Toast.LENGTH_SHORT, this);
                return false;
            }
        }

        if (TextUtils.isEmpty(address)) {
            MyUtils.showToast("商铺地址不能为空", Toast.LENGTH_SHORT, this);
            return false;
        }

        return true;
    }

    private void updateShop() {

//        String json = com.sharedream.wifiguard.cmdws.CmdUpdateShop.createRequestJson(accessToken, myShop.shopId, shopName, shopAddr, lng, lat, shopPhone, "", category, Integer.parseInt(cityId), shopPerson, groupId, logoSrc);
//        LogUtils.d("modify shop request >>>>> " + json);
//        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_UPDATE_SHOP, json, new MyCmdHttpTask.CmdListener() {
//            @Override
//            public void onCmdExecuted(String responseResult) {
//                if (!TextUtils.isEmpty(responseResult)) {
//                    LogUtils.d("modify shop response >>>>> " + responseResult);
//                    handleUpdateShopResults(responseResult);
//                }
//            }
//
//            @Override
//            public void onCmdException(Throwable exception) {
//                LogUtils.d("modify shop exception >>>>> " + exception.getMessage());
//            }
//        });

        //        String json = CmdUpdateShop.createRequestJson(myShop.shopId, shopName, Integer.parseInt(cityId), shopAddr, shopPhone, lng, lat, category);
        //        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_UPDATE_SHOP, json, new BaseCmdHttpTask.CmdListener() {
        //            @Override
        //            public void onCmdExecuted(String responseResult) {
        //                if (!TextUtils.isEmpty(responseResult)) {
        //                    LogUtils.d("modify shop response >>>>> " + responseResult);
        //                    handleUpdateShopResults(responseResult);
        //                }
        //            }
        //
        //            @Override
        //            public void onCmdException(Exception exception) {
        //
        //            }
        //        });
    }

    private void handleUpdateShopResults(String responseResult) {
        //        CmdUpdateShop.Results results = CmdUpdateShop.parseResponseJson(responseResult);
        com.sharedream.wifiguard.cmdws.CmdUpdateShop.Results results = com.sharedream.wifiguard.cmdws.CmdUpdateShop.parseResponseJson(responseResult);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            MyUtils.showToast(results.msg, this);
            setResult(RESULT_OK);
            this.finish();
        } else if (results.code == -2) {
            MyUtils.showToast(results.msg, this);
        } else if (results.code == -1) {
            MyUtils.showToast(results.msg, this);
        }
    }

    private void startLocationActivity() {
        LocationActivity.launch(ShopDetailActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.SERVER_SUCCESS_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String province = bundle.getString(Constant.BUNDLE_KEY_LOC_PROVINCE);
            String city = bundle.getString(Constant.BUNDLE_KEY_LOC_CITY);
            String district = bundle.getString(Constant.BUNDLE_KEY_LOC_DISTRICT);
            String street = bundle.getString(Constant.BUNDLE_KEY_LOC_STREET);
            String streetNumber = bundle.getString(Constant.BUNDLE_KEY_LOC_STREET_NUMBER);
            String address = province + city + district + street + streetNumber;

            lng = bundle.getDouble(Constant.BUNDLE_KEY_LNG);
            lat = bundle.getDouble(Constant.BUNDLE_KEY_LAT);
            LogUtils.d("modify lnglat >>> " + lng + "," + lat);
            etShopAddress.setText(address);
        } else if (requestCode == Constant.RESULT_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            Picasso.with(AppContext.getContext()).load(imageUri).resize(100, 100).centerCrop().into(ivManagerAddShopLogo);
//            Cursor cursor = resolver.query(imageUri, null, null, null, null);
//            int column_index = cursor != null ? cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA) : 0;
//            if (cursor != null && cursor.moveToFirst()) {
//                uploadFilePath = cursor.getString(column_index);
//                cursor.close();
//            }
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11) {
                uploadFilePath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, data.getData());
            }
            // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT <= 22) {
                uploadFilePath = RealPathUtil.getRealPathFromURI_API11to22(this, data.getData());
                if (uploadFilePath == null) {
                    uploadFilePath = RealPathUtil.getRealPathFromURI_API20(this, data.getData());
                }
            }
            // SDK > 19 (Android 4.4)
            else {
                uploadFilePath = RealPathUtil.getRealPathFromURI_API20(this, data.getData());
            }
        } else if (requestCode == RESULT_FIRST_USER && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void showBigCategoryDialog() {
        final CategoryDialog categoryDialog = new CategoryDialog(this, R.style.CustomDialogStyle);
        categoryDialog.show();
        Window window = categoryDialog.getWindow();
        window.setWindowAnimations(R.style.CustomDialogAnimationStyle);

        ListView lvCategory = (ListView) categoryDialog.findViewById(R.id.lv_category);
        CategoryListAdapter categoryListAdapter = new CategoryListAdapter(bigCategoryList, null);
        lvCategory.setAdapter(categoryListAdapter);

        lvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CmdShopCategory.BigCategory bigCategory = bigCategoryList.get(position);
                smallCategoryList = DatabaseManager.queryAllSmallScene(bigCategory.id);
                etShopCategoryBig.setText(bigCategory.name);
                etShopCategorySmall.setText(smallCategoryList.get(0).name);
                GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, smallCategoryList.get(0).id);
                LogUtils.d("scene >>>>> " + smallCategoryList.get(0).name + ":" + smallCategoryList.get(0).id);
                categoryDialog.dismiss();
            }
        });

    }

    private void showSmallCategoryDialog() {
        final CategoryDialog categoryDialog = new CategoryDialog(this, R.style.CustomDialogStyle);
        categoryDialog.show();
        Window window = categoryDialog.getWindow();
        window.setWindowAnimations(R.style.CustomDialogAnimationStyle);

        ListView lvCategory = (ListView) categoryDialog.findViewById(R.id.lv_category);
        categoryListAdapter = new CategoryListAdapter(null, smallCategoryList);
        lvCategory.setAdapter(categoryListAdapter);

        lvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CmdShopCategory.SmallCategory smallCategory = smallCategoryList.get(position);
                etShopCategorySmall.setText(smallCategory.name);
                GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, smallCategory.id);
                LogUtils.d("scene >>>>> " + smallCategory.name + ":" + smallCategory.id);
                categoryDialog.dismiss();
            }
        });
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_shop_detail;
    }

    @Override
    public String getActivityTitle() {
        return "商铺信息";
    }
}
