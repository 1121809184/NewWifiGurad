package com.sharedream.wifiguard.cmdws;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.sharedream.wifiguard.utils.CmdUtil;

import java.util.HashMap;
import java.util.Map;


public abstract class MyCmdHttpTask {

    public final static int TIMEOUT_MS = 1000 * 5;
    public final static int TIMEOUT_RETRY_TIMES = 3;

    public final void sendHttpRequest(String url, String jsonData, final CmdListener cmdListener) {
        //LogUtils.d("未经加密的json -------> " + jsonData);
        //MyLog.debug(getClass(), "requestJson >>> " + jsonData);
        final String requestJsonEncrypted = encryptJson(jsonData);
        //MyLog.debug(getClass(), "requestJsonEncrypted >>> " + requestJsonEncrypted);
        //MyLog.saveLog2File(MyUtils.getCurrentDateTime() + " requestJson >>> " + url + " with " + jsonData);

        //LogUtils.d("已经加密的json -------> " +requestJsonEncrypted);
        StringRequest stringRequest = new MyStringRequest(requestJsonEncrypted, url, new ResponseListener(cmdListener), new ResponseErrorListener(cmdListener));
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

        void onCmdException(Throwable exception);
    }


    //正常通信
    public class ResponseListener implements Response.Listener<String>{
        CmdListener cmdListener;

        public ResponseListener(CmdListener cmdListener) {
            this.cmdListener = cmdListener;
        }

        @Override
        public void onResponse(String response) {
            if (response != null) {
                //MyLog.debug(getClass(), "responseJson <<< " + response);
                //MyLog.saveLog2File(MyUtils.getCurrentDateTime() + " responseJson <<< " + response);

                try {
                    String result = decryptJson(response);
                    //MyLog.debug(getClass(), "responseJsonDecrypted <<< " + result);
                    //String decryAndDecmpress = new String(MyUtils.decompress(Base64.decode(result, Base64.DEFAULT)));
                    if (cmdListener != null) {
                        cmdListener.onCmdExecuted(result);
                    }
                    //MyLog.debug(getClass(), "responseJsonDecryptedAndDecompress <<< " + decryAndDecmpress);
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

    //非正常
    public class ResponseErrorListener implements Response.ErrorListener{
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

    //自定义MyStringRequest
    public class MyStringRequest extends StringRequest{
        private String json;

        public MyStringRequest(String json, String url, Response.Listener<String> listener,
                               Response.ErrorListener errorListener) {
            super(Method.POST, url, listener, errorListener);
            this.json = json;
        }

        @Override
        public byte[] getBody() throws AuthFailureError {
            return json.getBytes();
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> map = new HashMap<String, String>();
            map.put("Content-Type", "application/json");
            return map;
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            Map<String, String> headersMap = response.headers;
            String bug = headersMap.get("ip");
            if (bug != null) {
                //MyLog.info(getClass(), "Bug" + bug);
                //MyLog.saveBugLog(bug);
            }
            return super.parseNetworkResponse(response);
        }
    }
}