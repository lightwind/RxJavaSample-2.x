package com.ly.rxjavasample_2x.fragment.elementary;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.ly.rxjavasample_2x.BaseFragment;
import com.ly.rxjavasample_2x.R;
import com.ly.rxjavasample_2x.adapter.ZhuangbiListAdapter;
import com.ly.rxjavasample_2x.model.ZhuangbiImage;
import com.ly.rxjavasample_2x.network.Network;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Create by LiuYang on 2018/7/13 15:05
 */
public class ElementaryFragment extends BaseFragment {

    @BindView(R.id.gridRv)
    RecyclerView mGridRv;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    Unbinder unbinder;

    ZhuangbiListAdapter mAdapter = new ZhuangbiListAdapter();

    @OnCheckedChanged({R.id.searchRb1, R.id.searchRb2, R.id.searchRb3, R.id.searchRb4})
    void onTagChecked(RadioButton searchRb, boolean checked) {
        if (checked) {
            unsubscribe();
            mAdapter.setImages(null);
            mSwipeRefreshLayout.setRefreshing(true);
            search(searchRb.getText().toString());
        }
    }

    private void search(String key) {
        mDisposable = Network.getZhuangbiApi()
                .search(key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<ZhuangbiImage>>() {
                    @Override
                    public void accept(List<ZhuangbiImage> zhuangbiImages) throws Exception {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mAdapter.setImages(zhuangbiImages);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), R.string.loading_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_elementary, container, false);
        unbinder = ButterKnife.bind(this, view);
        mGridRv.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mGridRv.setAdapter(mAdapter);
        mSwipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW);
        mSwipeRefreshLayout.setEnabled(false);
        return view;
    }

    @Override
    protected int getTitleRes() {
        return R.string.title_elementary;
    }

    @Override
    protected int getDialogRes() {
        return R.layout.dialog_elementary;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
