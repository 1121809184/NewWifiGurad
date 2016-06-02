package com.sharedream.wifiguard.version;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;


@SuppressWarnings("deprecation")
public class FileDownloadTask extends AsyncTask<String, Integer, Integer> {
    private static final int DOWNLOAD_RESULT_SUCCESS = 1;
    private static final int DOWNLOAD_RESULT_FAILURE = -1;
    private static final int FILE_LENGTH_UNKNOW = -2;
    private int idNotification;
    private long fileLength;
    private String filename;
    private String fileLengthMb;
    private String mimeTypeFromServer;
    private String folderPath;
    private String url;
    private String title;
    private File file;
    private Context context;
    private FileDownloadService service;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    public FileDownloadTask(String folderPath, Context context) {
        this(null, folderPath, null, context, null);
    }

    public FileDownloadTask(String title, String folderPath, String filename, Context context, FileDownloadService service) {
        this.title = title;
        this.folderPath = folderPath;
        this.filename = filename;
        this.context = context;
        this.service = service;
    }

    @Override
    protected void onPreExecute() {
        idNotification = createNotificationId();
        LogUtils.d("idNotification === " + idNotification);

        initNotification();
        MyUtils.showToast("开始下载", Toast.LENGTH_SHORT,context);
    }

    private int createNotificationId() {
        long systemTime = System.currentTimeMillis();
        String strSystemTime = String.valueOf(systemTime);
        int len = strSystemTime.length();
        return Integer.parseInt(strSystemTime.substring(len - 8, len));
    }

    private void initNotification() {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(context);
        builder.setTicker("文件下载");
        builder.setContentTitle("文件下载");
        builder.setSmallIcon(R.drawable.sdk_small);
        builder.setOnlyAlertOnce(true);

        Intent intent = new Intent(context, NotificationDialogActivity.class);
        PendingIntent pendintIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendintIntent);

        notificationManager.notify(idNotification, builder.build());
        if (service != null) {
            service.startForeground(idNotification, builder.build());
        }
    }

    @Override
    protected Integer doInBackground(String... params) {
        int downloadResult = DOWNLOAD_RESULT_FAILURE;
        InputStream is = null;
        FileOutputStream fos = null;
        HttpEntity httpEntity = null;
        RandomAccessFile raFile = null;
        url = params[0];

        long readLength = 0;
        FileDownload fileDownload = null;

        try {
            fileDownload = DatabaseManager.queryFileDownloadDataByUrl(url);
            boolean isContinueDownload = false;

            if (fileDownload != null) {
                filename = fileDownload.getFilename();

                file = new File(fileDownload.getSavePath());
                readLength = file.length();
                long totalLength = fileDownload.getTotalLength();

                LogUtils.d(filename + ": " + readLength + " / " + totalLength + " - " + fileDownload.getFinishFlag());

                isContinueDownload = readLength < totalLength && "N".equals(fileDownload.getFinishFlag());
            }

            HttpClient httpClient = new DefaultHttpClient();
            LogUtils.d("HttpClient执行了");
            HttpParams httpParams = httpClient.getParams();
            httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
            httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
            httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
            httpParams.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, Constant.SYS_ENCODING);

            HttpGet httpGet = new HttpGet(url);
            if (isContinueDownload && readLength > 0) {
                httpGet.addHeader("RANGE", "bytes=" + readLength + "-");
            }

            HttpResponse httpResponse = httpClient.execute(httpGet);
            mimeTypeFromServer = getMimeType(httpResponse);
            httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

            if (isContinueDownload) {
                fileLength = httpEntity.getContentLength() + readLength;
                raFile = new RandomAccessFile(file, "rw");
                raFile.seek(readLength);
            } else {
                if (filename == null) {
                    filename = getFilename(httpResponse, url);
                }
                file = createFilePath(folderPath, filename);

                fileLength = httpEntity.getContentLength();
                fos = new FileOutputStream(file);
                readLength = 0;
            }

            if (fileLength > 0) {
                fileLengthMb = convertFileLength2Mb(fileLength);
            } else {
                fileLengthMb = context.getString(R.string.notification_file_length_unknow);
                updateProgress(FILE_LENGTH_UNKNOW);
            }

            int len;
            byte[] buffer = new byte[1024 * 4];
            while ((len = is.read(buffer)) != -1) {
                readLength += len;
                if (fileLength > 0) {
                    int progress = (int) (readLength * 100 / fileLength);
                    updateProgress(progress);
                }

                if (isContinueDownload) {
                    raFile.write(buffer, 0, len);
                } else {
                    fos.write(buffer, 0, len);
                }

                if (isCancelled()) {
                    dismissNotification();

                    saveCurrentFileLength2Sqlite(readLength, url, fileDownload == null);
                    return DOWNLOAD_RESULT_FAILURE;
                }
            }

            downloadResult = readLength == fileLength ? DOWNLOAD_RESULT_SUCCESS : DOWNLOAD_RESULT_FAILURE;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpEntity != null) {
                try {
                    httpEntity.consumeContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (raFile != null) {
                try {
                    raFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!isCancelled()) {

                saveCurrentFileLength2Sqlite(readLength, url, fileDownload == null);
            }
        }

        return downloadResult;
    }

    private void saveCurrentFileLength2Sqlite(long readLength, String url, boolean firstDownload) {
        String finishFlag = readLength == fileLength ? "Y" : "N";

        LogUtils.d("finishFlag finally is: " + finishFlag + "(" + readLength + ")");

        if (firstDownload) {

            DatabaseManager.insertFileDownloadData(url, filename, file.getAbsolutePath(), finishFlag, readLength, fileLength);
        } else {
            DatabaseManager.updateFileDownloadData(url, finishFlag, readLength);
        }
    }

    private void dismissNotification() {
        builder.setProgress(0, 0, false);
        builder.setAutoCancel(true);
        builder.setTicker(context.getString(R.string.notification_file_stop_download));
        builder.setSmallIcon(R.drawable.sdk_small);
        notificationManager.notify(idNotification, builder.build());
        notificationManager.cancel(idNotification);
    }

    private File createFilePath(String folder, String filename) {
        if (folder == null || filename == null) {
            return null;
        }

        File file = new File(folder, filename);
        return file;
    }

    private String convertFileLength2Mb(long fileLength) {
        float mb = fileLength * 1.0f / 1024 / 1024;
        float mbFinal = (float) (Math.round(mb * 100)) / 100;
        String strFileLength = String.valueOf(mbFinal) + context.getString(R.string.version_file_length_unit);
        return strFileLength;
    }

    private String getMimeType(HttpResponse httpResponse) {
        String headerContentType = null;
        Header header = httpResponse.getFirstHeader("Content-Type");
        if (header != null) {
            headerContentType = header.getValue();
        }
        return headerContentType;
    }

    private String getFilename(HttpResponse httpResponse, String url) {
        String filename = getFilenameFromHeader(httpResponse);
        if (filename == null) {
            filename = FileUtil.getFilename(url);
        }
        if (filename == null || filename.indexOf(".") == -1 || filename.length() > 48) {
            filename = getRandomFileName();
        }

        return filename;
    }

    private String getFilenameFromHeader(HttpResponse httpResponse) {
        Header contentHeader = httpResponse.getFirstHeader("Content-Disposition");
        String filename = null;
        if (contentHeader != null) {
            HeaderElement[] values = contentHeader.getElements();
            if (values.length == 1) {
                NameValuePair param = values[0].getParameterByName("filename");
                if (param != null) {
                    try {
                        filename = param.getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return filename;
    }

    private String getRandomFileName() {
        return String.valueOf(System.currentTimeMillis());
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
    }

    private void updateProgress(int progress) {
        if (progress == FILE_LENGTH_UNKNOW) {
            builder.setProgress(0, 0, true);
        } else {
            builder.setProgress(100, progress, false);
        }

        if (title == null) {
            title = filename;
        }
        String content = String.format(context.getString(R.string.notification_file_downloading), title, fileLengthMb);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.sdk_small);
        notificationManager.notify(idNotification, builder.build());
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (title == null) {
            title = filename;
        }

        if (result == DOWNLOAD_RESULT_SUCCESS) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String mimeType = FileUtil.getMimeType(filename);

            if (mimeType == null) {
                mimeType = mimeTypeFromServer;
            }
            //intent.setDataAndType(Uri.fromFile(file), mimeType);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            notificationManager.cancel(idNotification);
            context.startActivity(intent);

            LogUtils.d("start file: " + filename + " [" + mimeType + "]");
        } else {
            String content = String.format(context.getString(R.string.notification_file_download_failure), title);
            builder.setContentText(content);
            builder.setContentTitle("文件下载");
            builder.setSmallIcon(R.drawable.sdk_small);
            builder.setProgress(0, 0, false);
            builder.setContentIntent(null);
//			notificationManager.notify(createNotificationId(), builder.build());
            notificationManager.notify(idNotification, builder.build());
        }

        if (service != null) {
            service.finishTask(url);
            if (service.hasFinishAllTask()) {
                service.stopSelf();
                service = null;
            }
        }
    }
}
