package com.sharedream.wifiguard.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.sharedream.wifiguard.R;

public class CategoryDialog extends Dialog {

    public CategoryDialog(Context context) {
        super(context);
    }

    public CategoryDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CategoryDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_category);
    }
}
