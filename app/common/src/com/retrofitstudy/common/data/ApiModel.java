package com.retrofitstudy.common.data;

import com.google.gson.annotations.SerializedName;

public class ApiModel<T> {
    @SerializedName("error_code")
    public int mErrorCode; // 链接成功，但服务器异常
    @SerializedName("error_desc")
    public String mErrorDesc; // 服务器异常的描述

    public T data;
}
