package com.ly.rxjavasample_2x.fragment.token_advanced;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ly.rxjavasample_2x.BaseFragment;
import com.ly.rxjavasample_2x.R;
import com.ly.rxjavasample_2x.model.FakeThing;
import com.ly.rxjavasample_2x.model.FakeToken;
import com.ly.rxjavasample_2x.network.Network;
import com.ly.rxjavasample_2x.network.api.FakeApi;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class TokenAdvancedFragment extends BaseFragment {

    @BindView(R.id.tokenTv)
    TextView tokenTv;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    final FakeToken cachedFakeToken = new FakeToken(true);
    boolean tokenUpdated;

    @OnClick(R.id.invalidateTokenBt)
    void invalidateToken() {
        cachedFakeToken.expired = true;
        Toast.makeText(getActivity(), R.string.token_destroyed, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.requestBt)
    void upload() {
        tokenUpdated = false;
        swipeRefreshLayout.setRefreshing(true);
        unsubscribe();
        final FakeApi fakeApi = Network.getFakeApi();
        mDisposable = Observable.just(1)
                .flatMap(new Function<Object, Observable<FakeThing>>() {
                    @Override
                    public Observable<FakeThing> apply(Object o) {
                        return cachedFakeToken.token == null
                                ? Observable.<FakeThing>error(new NullPointerException("Token is null!"))
                                : fakeApi.getFakeData(cachedFakeToken);
                    }
                })
                .retryWhen(new Function<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> apply(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Function<Throwable, Observable<?>>() {
                            @Override
                            public Observable<?> apply(Throwable throwable) {
                                if (throwable instanceof IllegalArgumentException || throwable instanceof NullPointerException) {
                                    return fakeApi.getFakeToken("fake_auth_code")
                                            .doOnNext(new Consumer<FakeToken>() {
                                                @Override
                                                public void accept(FakeToken fakeToken) {
                                                    tokenUpdated = true;
                                                    cachedFakeToken.token = fakeToken.token;
                                                    cachedFakeToken.expired = fakeToken.expired;
                                                }
                                            });
                                }
                                return Observable.error(throwable);
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FakeThing>() {
                    @Override
                    public void accept(FakeThing fakeData) {
                        swipeRefreshLayout.setRefreshing(false);
                        String token = cachedFakeToken.token;
                        if (tokenUpdated) {
                            token += "(" + getString(R.string.updated) + ")";
                        }
                        tokenTv.setText(getString(R.string.got_token_and_data, token, fakeData.id, fakeData.name));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), R.string.loading_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_token_advanced, container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW);
        swipeRefreshLayout.setEnabled(false);
        return view;
    }

    @Override
    protected int getDialogRes() {
        return R.layout.dialog_token_advanced;
    }

    @Override
    protected int getTitleRes() {
        return R.string.title_token_advanced;
    }
}
