package com.sharedream.wifiguard.task;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sharedream.wifiguard.utils.CmdUtil;

public abstract class BaseCmdHttpTask {

    private final static int TIMEOUT_MS = 1000 * 10;
    private final static int TIMEOUT_RETRY_TIMES = 3;

    public final void sendHttpRequest(String url, String jsonData, final CmdListener cmdListener) {
        if (url == null || jsonData == null) {
            return;
        }
        final String requestJsonEncrypted = encryptJson(jsonData);
        StringRequest stringRequest = new MyStringRequest(requestJsonEncrypted, url, new ResponseListener(cmdListener), new ResponseErrorListener(cmdListener));
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, getTimeoutRetryTimes(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        CmdUtil.getRequestQueue().add(stringRequest);
    }

    protected abstract String encryptJson(String json);

    protected abstract String decryptJson(String responseData);

    protected int getTimeoutRetryTimes() {
        return TIMEOUT_RETRY_TIMES;
    }

    public interface CmdListener {
        void onCmdExecuted(String responseResult);

        void onCmdException(Exception exception);
    }

    public class ResponseListener implements Response.Listener<String> {
        CmdListener cmdListener;

        public ResponseListener(CmdListener cmdListener) {
            this.cmdListener = cmdListener;
        }

        @Override
        public void onResponse(String response) {
            if (response != null) {
                try {
                    String result = decryptJson(response);
                    if (cmdListener != null) {
                        cmdListener.onCmdExecuted(result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cmdListener != null) {
                        cmdListener.onCmdException(e);
                    }
                }
            } else {
                if (cmdListener != null) {
                    cmdListener.onCmdException(new Exception("Error! response is null"));
                }
            }
        }
    }

    public class ResponseErrorListener implements Response.ErrorListener {
        CmdListener cmdListener;

        public ResponseErrorListener(CmdListener cmdListener) {
            this.cmdListener = cmdListener;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (cmdListener != null) {
                cmdListener.onCmdException(error);
            }
        }
    }

    public class MyStringRequest extends StringRequest {
        private String json;

        public MyStringRequest(String json, String url, Response.Listener<String> listener,
                               Response.ErrorListener errorListener) {
            super(Request.Method.POST, url, listener, errorListener);
            this.json = json;
        }

        @Override
        public byte[] getBody() throws AuthFailureError {
            return json == null ? null : json.getBytes();
        }
    }
}