package com.ppfuns.filemanager.widget;

import android.app.Activity;
import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ppfuns.filemanager.R;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 李冰锋 on 2016/12/8 18:59.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.view
 */
public class PpfunsDialog {
    public final static String TAG = PpfunsDialog.class.getSimpleName();
    private AlertDialog mAlertDialog;
    private final TextView mTvTitle;
    private final LinearLayout mBtnContainer;
    private final RecyclerView mRecyclerView;
    private final ScrollView mScrollView;


    private PpfunsDialog(Builder builder) {
        /**
         * 初始化view
         */
        LinearLayout dialogView = (LinearLayout) View.inflate(builder.mContextWeakReference.get(), R.layout.dialog_layout, null);

        mRecyclerView = (RecyclerView) dialogView.findViewById(R.id.rcv_list);
        mScrollView = (ScrollView) dialogView.findViewById(R.id.sv_text);

        mBtnContainer = (LinearLayout) dialogView.findViewById(R.id.ll_btn_container);
        mTvTitle = (TextView) dialogView.findViewById(R.id.tv_dialog_title);

        mRecyclerView.setFocusable(false);
        /**
         * 按钮
         */
        if (!builder.dialogBtnMap.keySet().isEmpty()) {
            for (Pair<String, OnDialogBtnClickListener> pair : builder.dialogBtnMap.keySet()) {
                View btnConfirm = View.inflate(builder.mContextWeakReference.get(), R.layout.dialog_btn, null);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.weight = 1;
                btnConfirm.setLayoutParams(params);

                mBtnContainer.addView(btnConfirm);
                TextView btnDialog = (TextView) btnConfirm.findViewById(R.id.btn_dialog);

                btnDialog.setText(pair.first);
                if (builder.dialogBtnMap.get(pair)) {
                    btnDialog.requestFocus();
                }

                final OnDialogBtnClickListener onDialogBtnClickListener = pair.second;
                btnDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDialogBtnClickListener.onClick(PpfunsDialog.this);
                    }
                });
            }
        }

        /**
         * 标题
         */
        if (TextUtils.isEmpty(builder.title)) {
            mTvTitle.setVisibility(View.GONE);
        } else {
            mTvTitle.setVisibility(View.VISIBLE);
            mTvTitle.setText(builder.title);
        }

        /**
         * 消息
         */
        if (TextUtils.isEmpty(builder.msg)) {
            //因此純文本顯示
            mScrollView.setVisibility(View.GONE);

            //列表
            if (builder.mAdapter != null && builder.mLayoutManager != null) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setLayoutManager(builder.mLayoutManager);
                mRecyclerView.setAdapter(builder.mAdapter);
            }

        } else {
            mScrollView.setVisibility(View.VISIBLE);
            ((TextView) mScrollView.findViewById(R.id.tv_dialog_msg)).setText(builder.msg);
        }


        /**
         * 創建dialog
         */
        mAlertDialog = new AlertDialog.Builder(builder.mContextWeakReference.get(), R.style.PpfunsDialogStyle)
                .setView(dialogView)
                .setCancelable(builder.cancelable)
                .create();
    }


    public void show() {
        if (!mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
    }

    public boolean isShow() {
        return mAlertDialog != null && mAlertDialog.isShowing();
    }

    public void dismiss() {
        if (mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }

    public static Builder builder(Activity activity) {
        return new Builder(activity);
    }

    public static class Builder {

        private String title;
        private String msg;
        private WeakReference<Context> mContextWeakReference;

        private Map<Pair<String, OnDialogBtnClickListener>, Boolean> dialogBtnMap;
        private boolean cancelable;
        private RecyclerView.LayoutManager mLayoutManager;
        private RecyclerView.Adapter mAdapter;


        private Builder(Context context) {
            mContextWeakReference = new WeakReference<>(context);
            dialogBtnMap = new LinkedHashMap<>();
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder list(RecyclerView.Adapter pAdapter, RecyclerView.LayoutManager pLayoutManager) {
            mAdapter = pAdapter;
            mLayoutManager = pLayoutManager;

            return this;
        }

        public Builder confirmButton(String btnTxt, OnDialogBtnClickListener listener, boolean focus) {
            dialogBtnMap.put(new Pair<>(btnTxt, listener), focus);
            return this;
        }

        public Builder cancelButton(String btnTxt, OnDialogBtnClickListener listener, boolean focus) {
            dialogBtnMap.put(new Pair<>(btnTxt, listener), focus);
            return this;
        }

        public Builder cancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public PpfunsDialog build() {
            PpfunsDialog ppfunsDialog = new PpfunsDialog(this);
            clear();
            return ppfunsDialog;
        }

        public PpfunsDialog show() {
            PpfunsDialog ppfunsDialog = new PpfunsDialog(this);
            ppfunsDialog.show();
            clear();
            return ppfunsDialog;
        }

        private void clear() {
            dialogBtnMap.clear();
            mContextWeakReference.clear();
        }
    }

    public interface OnDialogBtnClickListener {
        void onClick(PpfunsDialog dialog);
    }


}
