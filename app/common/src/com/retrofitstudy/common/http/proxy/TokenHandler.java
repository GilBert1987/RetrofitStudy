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

package com.retrofitstudy.common.http.proxy;

import android.util.Log;

import com.retrofitstudy.common.data.DefaultTokenGenerator;
import com.retrofitstudy.common.data.ITokenGenerator;
import com.retrofitstudy.common.data.TokenManager;
import com.retrofitstudy.common.http.RetrofitUtil;
import com.retrofitstudy.common.data.TokenModel;
import com.retrofitstudy.common.exception.TokenInvalidException;
import com.retrofitstudy.common.exception.TokenNotExistException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class TokenHandler implements InvocationHandler {
    private ITokenGenerator mTokenGenerator = new DefaultTokenGenerator();
    private Object mProxyObject;
    private Throwable mRefreshTokenError;
    private int mRetrtTimes = 0;
    public TokenHandler(Object proxyObject) {
        mProxyObject = proxyObject;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Log.d(RetrofitUtil.TAG, "TokenHandler invoke");
        return Observable.just(null).flatMap(new Func1<Object, Observable<?>>() {
            @Override
            public Observable<?> call(Object o) {
                try {
                    try {
                        return (Observable<?>) method.invoke(mProxyObject, args);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> observable) {
                return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        if (throwable instanceof TokenInvalidException) {
                            if (mRetrtTimes < 1) {
                                return refreshTokenWhenTokenInvalid();
                            } else {
                                Observable.error(throwable);
                            }
                        } else if (throwable instanceof TokenNotExistException) {
                            return Observable.error(throwable);
                        }
                        return Observable.error(throwable);
                    }
                });
            }
        });
    }

    /**
     * Refresh the token when the current token is invalid.
     *
     * @return Observable
     */
    private Observable<?> refreshTokenWhenTokenInvalid() {
        synchronized (TokenHandler.class) {
            Log.d(RetrofitUtil.TAG, "TokenHandler refreshTokenWhenTokenInvalid");
            mRetrtTimes++;
            Observable.create(new Observable.OnSubscribe<TokenModel>() {
                @Override
                public void call(Subscriber<? super TokenModel> subscriber) {
                    TokenModel tokenModel =  new TokenModel();
                    tokenModel.token = mTokenGenerator.geneatorToken();
                    subscriber.onNext(tokenModel);
                }
            }).subscribe(new Subscriber<TokenModel>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    mRefreshTokenError = e;
                }

                @Override
                public void onNext(TokenModel tokenModel) {
                    mRefreshTokenError = null;
                    TokenManager.updateToken(tokenModel.token);
                    Log.d(RetrofitUtil.TAG, "TokenHandler refreshTokenWhenTokenInvalid success:" + tokenModel.token);
                }
            });
        }

        if (mRefreshTokenError != null) {
            return Observable.error(mRefreshTokenError);
        } else {
            return Observable.just(true);
        }
    }
}
