package com.retrofitstudy.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.retrofitstudy.common.http.RetrofitUtil;
import com.retrofitstudy.common.exception.TokenInvalidException;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import com.retrofitstudy.demo.api.IApiService;
import com.retrofitstudy.demo.api.ResultModel;
import com.retrofitstudy.demo.platform.R;

public class RetrofitTestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_retrofit_test);
        findViewById(R.id.btn_request_get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGet();
            }
        });

        findViewById(R.id.btn_request_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPost();
            }
        });
    }

    private void requestGet() {
        RetrofitUtil.getInstance()
                .get(IApiService.class)
                .getGetResult("498238400")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResultModel>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof TokenInvalidException) {
                            Toast.makeText(RetrofitTestActivity.this, "token need invalidate", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNext(ResultModel model) {
                        Toast.makeText(RetrofitTestActivity.this, model.mResponseFromServer, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void requestPost() {
        RetrofitUtil.getInstance()
                .get(IApiService.class)
                .getPostResult("498238400")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResultModel>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof TokenInvalidException) {
                            Toast.makeText(RetrofitTestActivity.this, "token need invalidate", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNext(ResultModel model) {
                        Toast.makeText(RetrofitTestActivity.this, model.mResponseFromServer, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
