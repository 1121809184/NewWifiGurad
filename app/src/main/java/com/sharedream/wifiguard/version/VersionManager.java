package com.sharedream.wifiguard.version;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdAppVersion;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.fragment.VerifyCenterFragment;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


public class VersionManager {
    private Activity activity;
    private File apkFolder;
    private ImageView ivCheckVersion;
    private TextView tvCheckTips;
    private boolean isCanCheck;

    public VersionManager(Activity activity, ImageView ivCheckVersion, TextView tvCheckTips, boolean isCanCheck) {
        this.activity = activity;
        this.ivCheckVersion = ivCheckVersion;
        this.tvCheckTips = tvCheckTips;
        this.isCanCheck = isCanCheck;
        apkFolder = FileUtil.getMpApkFileFolder(activity.getApplicationContext());
    }

    /**
     * 检测是否有新版本可更新
     */
    public void checkVersion() {
        Animation operatingAnim = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        if (operatingAnim != null) {
            if (isCanCheck) {
                ivCheckVersion.startAnimation(operatingAnim);
            }
        }
        String json = CmdAppVersion.createRequestJson();
        LogUtils.d("check version request >>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_CHECK_VERSION, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("check version reponse >>> " + responseResult);
                    handleCheckVersionResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("check version exception >>> " + exception);
            }
        });
    }

    private void handleCheckVersionResults(String response) {
        CmdAppVersion.Results results = CmdAppVersion.parseResponseJson(response);
        if (results == null) {
            return;
        }
        VerifyCenterFragment.hasCheckVersion = true;
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            if (isCanCheck) {
                ivCheckVersion.clearAnimation();
            }
            CmdAppVersion.Data data = results.data;
            if (data != null) {
                if (data.force == Constant.UPDATE_FORCEDLY) {
                    VerifyCenterFragment.hasCheckVersion = false;
                }
                updateNewVersion(data.version, data.logs, data.url, data.force, data.version);
                LogUtils.d("更新版本号 >>> " + data.version);
                LogUtils.d("更新LOG >>> " + data.logs);
                LogUtils.d("更新APK地址 >>> " + data.url);
                LogUtils.d("是否强制更新 >>> " + data.force);
                LogUtils.d("更新日期 >>> " + data.date);
            } else {
                if (isCanCheck) {
                    tvCheckTips.setVisibility(View.VISIBLE);
                }
            }
        } else {
            MyUtils.showToast(results.msg, Toast.LENGTH_SHORT, AppContext.getContext());
        }
    }

    /**
     * 弹出选择框，让用户选择是否升级新版本的客户端程序，是则下载新程序，否则取消（或强制退出）
     *
     * @param newVersion  新版本号
     * @param intro       新版本的改动信息
     * @param url         新版本的程序下载地址
     * @param forceFlag   是否强制升级
     * @param versionCode 版本代号
     * @return
     */
    public void updateNewVersion(String newVersion, String intro, String url, int forceFlag, String versionCode) {
        if (apkFolder != null && apkFolder.exists()) {
            File[] file = apkFolder.listFiles();
            String apkFileVersionCode = findApkFileVersionCode(file);
            if (apkFileVersionCode != null)
                checkApkFileVersionAndShowDialog(apkFileVersionCode, newVersion, intro, url, forceFlag, versionCode);
            else
                showDownloadDialog(newVersion, intro, url, forceFlag, versionCode);
        } else {
            showDownloadDialog(newVersion, intro, url, forceFlag, versionCode);
        }
    }

    private String findApkFileVersionCode(File[] file) {
        for (int i = 0; i < file.length; i++) {
            String fileName = file[i].getName();
            if (fileName.endsWith(Constant.VERSION_APK_EXT)) {
                String version = fileName.substring(0, fileName.lastIndexOf("."));
                return version;
            }
        }
        return null;
    }

    private void checkApkFileVersionAndShowDialog(String apkFileVersionCode, String newVersion, String intro, String url, int forceFlag, String versionCode) {
        if (versionCode.equals(apkFileVersionCode)) {
            File file = new File(apkFolder, versionCode + Constant.VERSION_APK_EXT);
            try {
                new ZipFile(file);
                showInstallDialog(newVersion, versionCode, forceFlag);
                return;
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        showDownloadDialog(newVersion, intro, url, forceFlag, versionCode);
    }

    private void showInstallDialog(final String version, final String versionCode, final int forceFlag) {
        if (activity == null || activity.isFinishing())
            return;

        final Dialog dialog = new VersionUpdateDialog(activity, R.style.CustomDialogStyle);
        dialog.show();
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.CustomDialogAnimationStyle);
        window.setGravity(Gravity.CENTER_VERTICAL);

        Button buttonUpdate = (Button) dialog.findViewById(R.id.btn_update);
        Button buttonCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        buttonUpdate.setText("立即安装");
        buttonCancel.setText("下次再说");
        TextView mUpdateContent = (TextView) dialog.findViewById(R.id.tv_content);
        TextView mTitle = (TextView) dialog.findViewById(R.id.tv_title);

        mUpdateContent.setText("版本已下载，请点击立即安装");
        mTitle.setText("安装新版本");
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(apkFolder, versionCode + Constant.VERSION_APK_EXT);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                activity.startActivity(intent);

                if (Constant.UPDATE_FORCEDLY == forceFlag) {
                    activity.finish();
                } else {
                    dialog.dismiss();
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constant.UPDATE_FORCEDLY == forceFlag) {
                    MyUtils.showToast("亲，您当前版本过低，请安装新版本", activity.getApplicationContext());
                } else {
                    dialog.dismiss();
                }
            }
        });
    }

    private void showDownloadDialog(final String newVersion, final String intro, final String url, final int forceFlag, final String versionCode) {
        if (activity == null || activity.isFinishing())
            return;

        final Dialog dialog = new VersionUpdateDialog(activity, R.style.CustomDialogStyle);
        dialog.show();
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.CustomDialogAnimationStyle);
        window.setGravity(Gravity.CENTER_VERTICAL);

        Button buttonUpdate = (Button) dialog.findViewById(R.id.btn_update);
        Button buttonCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        TextView mUpdateContent = (TextView) dialog.findViewById(R.id.tv_content);
        TextView mTitle = (TextView) dialog.findViewById(R.id.tv_title);

        if (intro == null || "".equals(intro) || "null".equals(intro)) {
            mUpdateContent.setText("哇，有新版本啦 \n赶紧升级新的版本体验一下吧");
        } else {
            mUpdateContent.setText(intro);
        }

        mTitle.setText("检测到新版本");

        final String downloadTitle = "安装新版本";

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderPath = apkFolder.getAbsolutePath();
                String filename = versionCode + Constant.VERSION_APK_EXT;

                Intent intent = new Intent(activity.getApplicationContext(), FileDownloadService.class);
                intent.putExtra(Constant.BUNDLE_KEY_DOWNLOAD_TITLE, downloadTitle);
                intent.putExtra(Constant.BUNDLE_KEY_FOLDER_PATH, folderPath);
                intent.putExtra(Constant.BUNDLE_KEY_FILENAME, filename);
                intent.putExtra(Constant.BUNDLE_KEY_DOWNLOAD_URL, url);
                activity.getApplicationContext().startService(intent);

                if (Constant.UPDATE_FORCEDLY == forceFlag) {
                    activity.finish();
                } else {
                    dialog.dismiss();
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constant.UPDATE_FORCEDLY == forceFlag) {
                    MyUtils.showToast("哇，有新版本啦\n赶紧升级新的版本体验一下吧", activity.getApplicationContext());
                } else {
                    dialog.dismiss();
                }
            }
        });
    }
}
