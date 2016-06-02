package com.sharedream.wifiguard.http;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.sharedream.wifiguard.app.AppContext;

public class MyCmdUtil {

    private static RequestQueue requestQueue;

	public static RequestQueue getRequestQueue() {
		if (requestQueue == null) {
			requestQueue = Volley.newRequestQueue(AppContext.getContext());
		}
		return requestQueue;
	}

    public static void sendRandomTagRequest(String url, String json, MyCmdHttpTask.CmdListener listener) {
        new MyRandomTagCmdHttpTask().sendHttpRequest(url,json,listener);
    }
}
