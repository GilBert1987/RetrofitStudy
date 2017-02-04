/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.retrofitstudy.common.http.converter;

import com.google.gson.TypeAdapter;
import com.retrofitstudy.common.data.ApiModel;
import com.retrofitstudy.common.data.ErrorCode;
import com.retrofitstudy.common.exception.ApiException;
import com.retrofitstudy.common.exception.TokenInvalidException;
import com.retrofitstudy.common.exception.TokenNotExistException;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Converter;

final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, Object> {

    private final TypeAdapter<T> adapter;

    GsonResponseBodyConverter(TypeAdapter<T> adapter) {
        this.adapter = adapter;
    }

    @Override
    public Object convert(ResponseBody value) throws IOException {
        try {
            ApiModel apiModel = (ApiModel) adapter.fromJson(value.charStream());
            if (apiModel.mErrorCode == ErrorCode.TOKEN_NOT_EXIST) {
                throw new TokenNotExistException();
            } else if (apiModel.mErrorCode == ErrorCode.TOKEN_INVALID) {
                throw new TokenInvalidException();
            } else if (apiModel.mErrorCode != 0) {
                // 特定 API 的错误，在相应的 Subscriber 的 onError 的方法中进行处理
                throw new ApiException();
            } else {
                return apiModel.data;
            }
        } finally {
            value.close();
        }}
}
