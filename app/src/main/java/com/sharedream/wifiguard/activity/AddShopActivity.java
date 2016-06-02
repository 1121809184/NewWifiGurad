package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.adapter.CategoryListAdapter;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdCreateToken;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AddShopActivity extends BaseActivity {
    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";
    private TextView tvUseAddress;
    private Button btnNextBinding;
    private ProgressDialog progressDialog;
    private EditText etShopName;
    private EditText etShopPhone;
    private EditText etShopPerson;
    private TextView etShopCategoryBig;
    private TextView etShopCategorySmall;
    private RelativeLayout rlShopCategoryShop;
    private RelativeLayout rlShopCategorySmall;
    private EditText etShopAddress;
    private ImageView ivShopNameStar;
    private ImageView ivAddressStar;
    private List<CmdShopCategory.BigCategory> bigCategoryList;
    private List<CmdShopCategory.SmallCategory> smallCategoryList;
    private CategoryListAdapter categoryListAdapter;
    private String shopName;
    private boolean saveToSQLite;
    private boolean noFirstLaunch;
    private double lng;
    private double lat;
    private ImageView ivManagerAddShopLogo;
    private String uploadFilePath;
    private String phone;
    private String owner;
    private String address;
    private String cityId;
    private int category;
    private String accessKey = "VBvN33BnrqcaAqrbOHLD4EbTuIwxwjA_VSdUo6B5";
    private String secretKey = "fdSgoq2c71UX8rJ8luX5zf3BsC03TmDmbOdKnr7e";
    private UploadManager uploadManager = new UploadManager();
    private ProgressDialog pd;
    private String uptoken;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, AddShopActivity.class);
        activity.startActivity(intent);
        AppContext.getContext().addActivity(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        tvUseAddress = ((TextView) findViewById(R.id.tv_use_address));
        btnNextBinding = ((Button) findViewById(R.id.btn_shop_save));
        etShopName = ((EditText) findViewById(R.id.et_shop_name));
        etShopPhone = ((EditText) findViewById(R.id.et_shop_phone));
        etShopPerson = ((EditText) findViewById(R.id.et_shop_person));
        etShopAddress = ((EditText) findViewById(R.id.et_shop_address));
        etShopCategoryBig = ((TextView) findViewById(R.id.et_shop_category_big));
        etShopCategorySmall = ((TextView) findViewById(R.id.et_shop_category_small));
        rlShopCategoryShop = ((RelativeLayout) findViewById(R.id.rl_shop_category_big));
        rlShopCategorySmall = ((RelativeLayout) findViewById(R.id.rl_shop_category_small));
        ivManagerAddShopLogo = ((ImageView) findViewById(R.id.iv_manager_add_shop_logo));
        ivShopNameStar = ((ImageView) findViewById(R.id.iv_shop_name_star));
        ivAddressStar = ((ImageView) findViewById(R.id.iv_address_star));

        btnNextBinding.setText("下一步");
        progressDialog = new ProgressDialog(AddShopActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setMessage("正在加载，请稍后.....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        addTextWatcher();
    }

    /*private void getShopCategoryFromServer() {
        LogUtils.d("从服务器获取分类信息");
        progressDialog.show();
        String accessKey = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_KEY, null);
        String uid = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_USER_ID, null);
        String json = com.sharedream.wifiguard.cmd.CmdShopCategory.createRequestJson(uid, accessKey);
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
        com.sharedream.wifiguard.cmd.CmdShopCategory.Results results = com.sharedream.wifiguard.cmd.CmdShopCategory.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            bigCategoryList = results.data;
            if (bigCategoryList != null) {
                LogUtils.d("下载大场景 >>>> bigCategoryList:" + bigCategoryList);
                com.sharedream.wifiguard.cmd.CmdShopCategory.BigCategory bigCategory = bigCategoryList.get(0);
                etShopCategoryBig.setText(bigCategory.name);
                smallCategoryList = bigCategoryList.get(0).children;
                if (smallCategoryList != null) {
                    categoryListAdapter = new CategoryListAdapter(null, smallCategoryList);
                    com.sharedream.wifiguard.cmd.CmdShopCategory.SmallCategory smallCategory = smallCategoryList.get(0);
                    etShopCategorySmall.setText(smallCategory.name);
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
                }
            }).start();
        }
    }*/

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
        lng = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LNG, 0);
        lat = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LAT, 0);

        noFirstLaunch = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.SP_KEY_FIRST_LAUNCH, false);
        bigCategoryList = new ArrayList<CmdShopCategory.BigCategory>();
        initCurrentAddress();

        saveToSQLite = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.SP_KEY_SAVE_TO_DB, false);
        if (saveToSQLite) {
            getShopCategoryFromSQLite();
        } else {
            getShopCategoryFromServerUseNewInterface();
        }
    }

    private void getShopCategoryFromServerUseNewInterface() {
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = CmdShopCategory.createRequestJson(accessToken);
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
        CmdShopCategory.Results results = CmdShopCategory.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            bigCategoryList = results.data;
            if (bigCategoryList != null) {
                CmdShopCategory.BigCategory bigCategory = bigCategoryList.get(0);
                etShopCategoryBig.setText(bigCategory.name);
                smallCategoryList = bigCategoryList.get(0).children;
                if (smallCategoryList != null) {
                    categoryListAdapter = new CategoryListAdapter(null, smallCategoryList);
                    CmdShopCategory.SmallCategory smallCategory = smallCategoryList.get(0);
                    etShopCategorySmall.setText(smallCategory.name);
                    LogUtils.d("small category >>>>> " + smallCategory.name + ":" + smallCategory.id);
                    GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, smallCategory.id);
                }
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(100);
                    saveShopCategoryToSQLite();
                    GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SAVE_TO_DB, true);
                }
            }).start();
        }
    }

    private void initCurrentAddress() {
        String province = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_PROVINCE, null);
        String city = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_CITY, null);
        String district = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_DISTRICT, null);
        String street = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_STREET, null);
        String streetNumber = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_STREET_NUMBER, null);
        String address = province + city + district + street + streetNumber;
        etShopAddress.setText(address);
    }

    private void getShopCategoryFromSQLite() {
        bigCategoryList = DatabaseManager.queryAllBigScene();
        if (bigCategoryList != null) {
            CmdShopCategory.BigCategory bigCategory = bigCategoryList.get(0);
            etShopCategoryBig.setText(bigCategory.name);
            smallCategoryList = DatabaseManager.queryAllSmallScene(bigCategory.id);
            if (smallCategoryList != null) {
                categoryListAdapter = new CategoryListAdapter(null, smallCategoryList);
                CmdShopCategory.SmallCategory smallCategory = smallCategoryList.get(0);
                etShopCategorySmall.setText(smallCategory.name);
                LogUtils.d("small scene >>>>> " + smallCategory.name + ":" + smallCategory.id);
                GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, smallCategory.id);
            }
        }
    }

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

    private void setListener() {
        tvUseAddress.setOnClickListener(this);
        btnNextBinding.setOnClickListener(this);
        rlShopCategoryShop.setOnClickListener(this);
        rlShopCategorySmall.setOnClickListener(this);
        ivManagerAddShopLogo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.tv_use_address:
                startLocationActivity();
                break;
            case R.id.btn_shop_save:
                startBindingWifiActivity();
                break;
            case R.id.rl_shop_category_big:
                showBigCategoryDialog();
                break;
            case R.id.rl_shop_category_small:
                showSmallCategoryDialog();
                break;
            case R.id.iv_manager_add_shop_logo:
                selectImageAndUpload();
                break;
        }
    }

    private void selectImageAndUpload() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, Constant.RESULT_CODE_PICK_IMAGE);
    }

    private void showBigCategoryDialog() {
        final CategoryDialog categoryDialog = new CategoryDialog(AddShopActivity.this, R.style.CustomDialogStyle);
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
                if (saveToSQLite) {
                    smallCategoryList = DatabaseManager.queryAllSmallScene(bigCategory.id);
                } else {
                    smallCategoryList = bigCategory.children;
                }
                etShopCategoryBig.setText(bigCategory.name);
                etShopCategorySmall.setText(smallCategoryList.get(0).name);
                LogUtils.d("small scene >>>>> " + smallCategoryList.get(0).name + ":" + smallCategoryList.get(0).id);
                GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, smallCategoryList.get(0).id);
                rlShopCategorySmall.setEnabled(true);
                categoryDialog.dismiss();
            }
        });
    }

    private void showSmallCategoryDialog() {
        final CategoryDialog categoryDialog = new CategoryDialog(AddShopActivity.this, R.style.CustomDialogStyle);
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
                LogUtils.d("最小分类 >>>>> " + smallCategory.name + ":" + smallCategory.id);
                categoryDialog.dismiss();
            }
        });
    }

    private boolean checkInput(String shopName, String phone, String owner, String uploadFilePath) {
        if (TextUtils.isEmpty(uploadFilePath)) {
            MyUtils.showToast(AppContext.getContext().getResources().getString(R.string.activity_add_shop_logo_null), Toast.LENGTH_SHORT, AddShopActivity.this);
            return false;
        }

        if (TextUtils.isEmpty(shopName)) {
            MyUtils.showToast(AppContext.getContext().getResources().getString(R.string.activity_add_shop_name_null), Toast.LENGTH_SHORT, AddShopActivity.this);
            return false;
        } else {
            Pattern pa = Pattern.compile("!|！|\\?|？|@|◎|#|＃|￥|%|％|……|※|×|_|——|＋|§", Pattern.CASE_INSENSITIVE);
            Matcher ma = pa.matcher(shopName);
            if (ma.find() || owner.contains(" ")) {
                MyUtils.showToast("商铺名称不能包含非法字符", Toast.LENGTH_SHORT, AddShopActivity.this);
                return false;
            }
        }

//        if (!TextUtils.isEmpty(phone)) {
//            Pattern p = Pattern.compile("^[1]+[3,5,8,4]+\\d{9}");
//            //Pattern p = Pattern.compile("[0-9]+");
//            Matcher m = p.matcher(phone);
//            if (!m.matches()) {
//                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_err);
//                MyUtils.showToast(str, Toast.LENGTH_SHORT, AddShopActivity.this);
//                return false;
//            }
//        }

        if (!TextUtils.isEmpty(owner)) {
            Pattern pa = Pattern.compile("!|！|\\?|？|@|◎|#|＃|(\\$)|￥|%|％|(\\^)|……|(\\&)|※|(\\*)|×|(\\()|（|(\\))|）|_|——|(\\+)|＋|(\\|)|§", Pattern.CASE_INSENSITIVE);
            Matcher ma = pa.matcher(owner);
            if (ma.find() || owner.contains(" ")) {
                MyUtils.showToast("联系人姓名不能包含非法字符", Toast.LENGTH_SHORT, AddShopActivity.this);
                return false;
            }
        }
        return true;
    }

    private void startBindingWifiActivity() {
        shopName = etShopName.getText().toString().trim();
        phone = etShopPhone.getText().toString().trim();
        owner = etShopPerson.getText().toString().trim();
        address = etShopAddress.getText().toString().trim();
        GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SHOP_PLACE, address);
        category = GlobalField.restoreFieldInt(AppContext.getContext(), Constant.SP_KEY_SMALL_CATEGORY_ID, 0);
        cityId = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_CITY_ID, null);
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);

        boolean isInputValid = checkInput(shopName, phone, owner, uploadFilePath);
        if (isInputValid) {
            getTokenFromServer();
        }

        //        String json = com.sharedream.wifiguard.cmdws.CmdAddShop.createRequestJson(accessToken, shopName, address, lng, lat, phone, "", category, Integer.parseInt(cityId), owner, "");
        //        LogUtils.d("add shop request >>>>> " + json);
        //        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_ADD_SHOP, json, new MyCmdHttpTask.CmdListener() {
        //            @Override
        //            public void onCmdExecuted(String responseResult) {
        //                if (!TextUtils.isEmpty(responseResult)) {
        //                    LogUtils.d("add shop response >>>>> " + responseResult);
        //                    handleAddShopResulte(responseResult);
        //                }
        //            }
        //
        //            @Override
        //            public void onCmdException(Throwable exception) {
        //                LogUtils.d("add shop exception >>>>> " + exception.getMessage());
        //            }
        //        });
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

    public byte[] HmacSHA1Encrypt(String encryptText, String encryptKey)
            throws Exception {
        byte[] data = encryptKey.getBytes(ENCODING);
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        Mac mac = Mac.getInstance(MAC_NAME);
        mac.init(secretKey);
        byte[] text = encryptText.getBytes(ENCODING);
        return mac.doFinal(text);
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

//        try {
//            uploadFile.createNewFile();
//            FileOutputStream fOut = null;
//            fOut = new FileOutputStream(uploadFile);
//            Bitmap photo = BitmapFactory.decodeFile(uploadFilePath);
//            if(photo.compress(Bitmap.CompressFormat.JPEG,30,fOut)){
//                fOut.flush();
//                fOut.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


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
                                String logoSrc = "http://" + domain + "/" + key + "?imageMogr2/quality/75";
                                GroupBelongsActivity.launch(AddShopActivity.this, shopName, address, lng, lat, phone, category, cityId, owner, logoSrc, Constant.BUNDLE_KEY_ADD_SHOP, 0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            MyUtils.showToast("图片保存失败", Toast.LENGTH_SHORT, AddShopActivity.this);
                        }
                    }

                }, uploadOptions);
    }

    //    private void handleAddShopResulte(String response) {
    //        CmdAddShop.Results results = CmdAddShop.parseResponseJson(response);
    //        if (results == null) {
    //            return;
    //        }
    //        if (results.code == Constant.SERVER_SUCCESS_CODE) {
    //            LogUtils.d("add shop return shopid >>>> " + results.data.shopId);
    //            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SHOP_ID, results.data.shopId);
    //            BindingWifiActivity.launch(AddShopActivity.this, shopName);
    //            MyUtils.showToast(results.msg, Toast.LENGTH_SHORT, this);
    //        } else {
    //            MyUtils.showToast(results.msg, Toast.LENGTH_SHORT, this);
    //        }
    //    }

    private void startLocationActivity() {
        LocationActivity.launch(AddShopActivity.this);
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
            lng = bundle.getDouble(Constant.BUNDLE_KEY_LNG);
            lat = bundle.getDouble(Constant.BUNDLE_KEY_LAT);
            String address = province + city + district + street + streetNumber;
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
            finish();
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_shop_detail;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_add_shop);
        return title;
    }
}
