package com.sharedream.wifiguard.version;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sharedream.wifiguard.R;

public class NotificationDialogActivity extends Activity implements OnClickListener {

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btn_yes:
			stop();
			break;
		case R.id.btn_no:
			finish();
			break;
		}
	}

	private void stop() {
		stopService();
		finish();
	}
	
	private void stopService() {
		stopService(new Intent(this, FileDownloadService.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_notification);
		
		Button buttonYes = (Button) findViewById(R.id.btn_yes);
		buttonYes.setOnClickListener(this);
		
		Button buttonNo = (Button) findViewById(R.id.btn_no);
		buttonNo.setOnClickListener(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}
}
