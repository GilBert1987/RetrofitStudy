package com.retrofitstudy.common.http;

import android.text.TextUtils;
import android.util.Log;

import com.retrofitstudy.common.data.TokenManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AddParamIterceptor implements Interceptor {
    private static final Map<String, String> sBaseParamMap = new HashMap<String, String>() {
        {
            put("versionName", "1.0.0");
            put("packageName", "com.mi.test");
            put("versionCode", "100001");
        }
    };

    @Override
    public Response intercept(Chain chain) throws IOException {
        Log.d(RetrofitUtil.TAG, "AddParamIterceptor intercept");
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder();

        HttpUrl url = original.url();
        switch (original.method()){
            case "GET":
                HttpUrl.Builder builder = url.newBuilder();
                for (Map.Entry<String, String> mapEntry : sBaseParamMap.entrySet()) {
                    if (!TextUtils.isEmpty(mapEntry.getValue())) {
                        builder.addQueryParameter(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
                if (!TextUtils.isEmpty(TokenManager.getToken())) {
                    builder.addQueryParameter("token", TokenManager.getToken());
                }
                url = builder.build();
                break;
            case "POST":
                if(original.body() instanceof FormBody) {
                    FormBody.Builder newFormBody = new FormBody.Builder();
                    FormBody oldFormBody = (FormBody) original.body();
                    for (int i = 0; i < oldFormBody.size(); i++){
                        newFormBody.addEncoded(oldFormBody.encodedName(i), oldFormBody.encodedValue(i));
                    }
                    for (Map.Entry<String, String> mapEntry : sBaseParamMap.entrySet()) {
                        if (!TextUtils.isEmpty(mapEntry.getValue())) {
                            newFormBody.add(mapEntry.getKey(), mapEntry.getValue());
                        }
                    }
                    if (!TextUtils.isEmpty(TokenManager.getToken())) {
                        newFormBody.add("token", TokenManager.getToken());
                    }
                    requestBuilder.method(original.method(),newFormBody.build());
                }
                break;
        }
        Request request = requestBuilder.url(url).build();
        return chain.proceed(request);
    }
}
