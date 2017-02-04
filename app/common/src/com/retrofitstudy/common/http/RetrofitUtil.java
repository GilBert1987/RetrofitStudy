/*
 * Copyright (C) 2016 david.wei (lighters)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.retrofitstudy.common.http;

import com.retrofitstudy.common.http.converter.GsonConverterFactory;
import com.retrofitstudy.common.http.proxy.TokenHandler;

import java.lang.reflect.Proxy;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

public class RetrofitUtil {
    public static final String TAG = "retofit";

    public static final String API = "http://10.235.234.109:8888/";

    private static Retrofit sRetrofit;
    private static OkHttpClient sOkHttpClient;
    private static RetrofitUtil instance;
    private final static Object mRetrofitLock = new Object();

    private static Retrofit getRetrofit() {
        if (sRetrofit == null) {
            synchronized (mRetrofitLock) {
                if (sRetrofit == null) {
                    OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();

                    HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    clientBuilder.addInterceptor(httpLoggingInterceptor);
                    clientBuilder.addInterceptor(new AddParamIterceptor());
                    clientBuilder.addInterceptor(new SignatureIterceptor());
                    sOkHttpClient = clientBuilder.build();
                    sRetrofit = new Retrofit.Builder().client(sOkHttpClient)
                        .baseUrl(API)
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                }
            }
        }
        return sRetrofit;
    }

    public void setRetrofit(Retrofit retrofit) {
        if (sRetrofit != null) {
            throw new IllegalStateException("set retrofit must be called before init");
        }
        sRetrofit = retrofit;
    }

    public static RetrofitUtil getInstance() {
        if (instance == null) {
            synchronized (RetrofitUtil.class) {
                if (instance == null) {
                    instance = new RetrofitUtil();
                }
            }
        }
        return instance;
    }

    public <T> T get(Class<T> tClass) {
        T t = getRetrofit().create(tClass);
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class<?>[] { tClass }, new TokenHandler(t));
    }
}
