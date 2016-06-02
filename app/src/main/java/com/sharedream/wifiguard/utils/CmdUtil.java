package com.sharedream.wifiguard.utils;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.task.BaseCmdHttpTask;
import com.sharedream.wifiguard.task.RandomTagCmdHttpTask;

public class CmdUtil {

    private static RequestQueue requestQueue;

	public static RequestQueue getRequestQueue() {
		if (requestQueue == null) {
			requestQueue = Volley.newRequestQueue(AppContext.getContext());
		}
		return requestQueue;
	}

    public static void sendRandomTagRequest(String url, String json, BaseCmdHttpTask.CmdListener listener) {
        new RandomTagCmdHttpTask().sendHttpRequest(url, json, listener);
    }
}
