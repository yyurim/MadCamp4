package com.madcamp.petclub.MainActivity;

import android.app.ProgressDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.madcamp.petclub.login.SharedPreference;

public class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    // 다시봐################################################################
    public String getUid() {
        return SharedPreference.getAttribute(getApplicationContext(), "userID");
    }


}