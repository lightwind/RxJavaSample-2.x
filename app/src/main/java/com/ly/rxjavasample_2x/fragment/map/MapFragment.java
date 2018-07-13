package com.ly.rxjavasample_2x.fragment.map;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ly.rxjavasample_2x.BaseFragment;
import com.ly.rxjavasample_2x.R;
import com.ly.rxjavasample_2x.adapter.ItemListAdapter;
import com.ly.rxjavasample_2x.model.Item;
import com.ly.rxjavasample_2x.network.Network;
import com.ly.rxjavasample_2x.util.GankBeautyResultToItemsMapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Create by LiuYang on 2018/7/13 15:26
 */
public class MapFragment extends BaseFragment {

    @BindView(R.id.pageTv)
    TextView mPageTv;
    @BindView(R.id.previousPageBt)
    AppCompatButton mPreviousPageBt;
    @BindView(R.id.gridRv)
    RecyclerView mGridRv;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    Unbinder unbinder;

    ItemListAdapter mAdapter = new ItemListAdapter();

    private int mPage = 0;

    @OnClick({R.id.previousPageBt, R.id.nextPageBt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.previousPageBt:
                loadPage(--mPage);
                if (mPage == 1) {
                    mPreviousPageBt.setEnabled(false);
                }
                break;
            case R.id.nextPageBt:
                loadPage(++mPage);
                if (mPage == 2) {
                    mPreviousPageBt.setEnabled(true);
                }
                break;
        }
    }

    private void loadPage(int page) {
        mSwipeRefreshLayout.setRefreshing(true);
        unsubscribe();
        mDisposable = Network.getGankApi()
                .getBeauties(10, page)
                .map(GankBeautyResultToItemsMapper.getInstance())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Item>>() {
                    @Override
                    public void accept(List<Item> items) throws Exception {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mPageTv.setText(getString(R.string.page_with_number, MapFragment.this.mPage));
                        mAdapter.setItems(items);
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
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        unbinder = ButterKnife.bind(this, view);
        mGridRv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mGridRv.setAdapter(mAdapter);
        mSwipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW);
        mSwipeRefreshLayout.setEnabled(false);
        return view;
    }

    @Override
    protected int getTitleRes() {
        return R.string.title_map;
    }

    @Override
    protected int getDialogRes() {
        return R.layout.dialog_map;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}