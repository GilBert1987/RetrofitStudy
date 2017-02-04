package com.retrofitstudy.mock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.retrofitstudy.common.http.AddParamIterceptor;
import com.retrofitstudy.common.http.RetrofitUtil;
import com.retrofitstudy.common.http.converter.GsonConverterFactory;
import com.retrofitstudy.demo.RetrofitTestActivity;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

public class MockEntryActivity extends Activity {
    private MockServer mMockServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mock_main);
        findViewById(R.id.btnServer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mMockServer = new MockServer();
                    mMockServer.start();
                    Toast.makeText(MockEntryActivity.this, "服务器打开成功", Toast.LENGTH_SHORT).show();
                    setMockRetrofit();
                } catch (IOException e) {
                    Toast.makeText(MockEntryActivity.this, "服务器打开失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btnMock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MockEntryActivity.this, RetrofitTestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setMockRetrofit() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(httpLoggingInterceptor);
        clientBuilder.addInterceptor(new AddParamIterceptor());
        Retrofit mockRetrofit = new Retrofit.Builder().client(clientBuilder.build())
                .baseUrl("http://localhost:9876/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitUtil.getInstance().setRetrofit(mockRetrofit);
    }
}