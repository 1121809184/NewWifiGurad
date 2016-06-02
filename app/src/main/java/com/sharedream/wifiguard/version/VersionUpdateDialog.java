package com.sharedream.wifiguard.version;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.sharedream.wifiguard.R;


public class VersionUpdateDialog extends Dialog {
	public VersionUpdateDialog(Context context) {
		super(context);
	}

	public VersionUpdateDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dialog_version_update);
	}
}