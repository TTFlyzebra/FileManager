package com.ppfuns.filemanager.widget;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ppfuns.filemanager.R;

/**
 * Created by 李冰锋 on 2016/9/29 14:26.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.widget
 */
public class SmbLoginDialog extends AlertDialog {
    public final static String TAG = SmbLoginDialog.class.getSimpleName();
    private EditText mEdtUsername;
    private EditText mEdtPassword;
    private Button mBtnConfirm;
    private DialogListener mListener;

    protected SmbLoginDialog(Context context) {
        this(context, 0);
    }

    protected SmbLoginDialog(Context context, int theme) {
        super(context, theme);

        View inflate = LayoutInflater.from(context).inflate(R.layout.dialog_login_input, null, false);
        mEdtUsername = (EditText) inflate.findViewById(R.id.edt_smb_login_username);
        mEdtPassword = (EditText) inflate.findViewById(R.id.edt_smb_login_password);
        mBtnConfirm = (Button) inflate.findViewById(R.id.btn_smb_login_confirm);

        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onConfirm(mEdtUsername.getText().toString(), mEdtPassword.getText().toString());
                }
            }
        });
    }

    public interface DialogListener {
        void onConfirm(String username, String password);
    }

}
