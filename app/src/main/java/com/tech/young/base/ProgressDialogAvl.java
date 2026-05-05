package com.tech.young.base;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import com.tech.young.R;

public class ProgressDialogAvl {
    private final Dialog dialog;

    public ProgressDialogAvl(Context context) {
        View view = View.inflate(context, R.layout.dialog_progress_avl, null);
        dialog = new Dialog(context, R.style.CustomDialogProgress);
        dialog.setContentView(view);
        dialog.setCancelable(false);
    }

    public void isLoading(boolean isLoading) {
        try {
            if (isLoading) {
                if (dialog != null && !dialog.isShowing()) {
                    dialog.show();
                }
            } else {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
