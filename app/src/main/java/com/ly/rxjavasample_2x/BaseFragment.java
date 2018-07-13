package com.ly.rxjavasample_2x;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;

import java.util.Objects;

import butterknife.OnClick;
import io.reactivex.disposables.Disposable;

/**
 * Create by LiuYang on 2018/7/13 15:04
 */
public abstract class BaseFragment extends Fragment {
    protected Disposable mDisposable;

    @OnClick(R.id.tipBt)
    void tip() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getTitleRes())
                .setView(Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(getDialogRes(), null))
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }

    protected void unsubscribe() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
    }

    protected abstract int getTitleRes();

    protected abstract int getDialogRes();
}
