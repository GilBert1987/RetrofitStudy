package com.retrofitstudy.common.http;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.retrofitstudy.common.utils.Coder;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SignatureIterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Log.d(RetrofitUtil.TAG, "SignatureIterceptor intercept");
        Request original = chain.request();
        Map<String, String> map = new TreeMap<>(
                new Comparator<String>() {
                    public int compare(String obj1, String obj2) {
                        return obj1.compareTo(obj2);
                    }
                });
        Request.Builder requestBuilder = original.newBuilder();

        HttpUrl url = original.url();
        String paramsSign;
        switch (original.method()){
            case "GET":
                Set<String> urlQueryParameterNames = url.queryParameterNames();

                if(urlQueryParameterNames != null && !urlQueryParameterNames.isEmpty()){
                    for (String key : urlQueryParameterNames) {
                        String finalStr = encode(url.queryParameter(key));
                        map.put(key,finalStr);
                    }
                }
                paramsSign = getParamsSign(map);
                HttpUrl.Builder builder = url.newBuilder();
                for (Map.Entry<String, String> entry: map.entrySet()) {
                    builder.setEncodedQueryParameter(entry.getKey(), entry.getValue());
                }
                builder.addQueryParameter("sign", paramsSign);
                url = builder.build();
                break;
            case "POST":
                FormBody formBody = (FormBody) original.body();
                for (int i = 0; i < formBody.size(); i++) {
                    String finalStr = encode(formBody.encodedValue(i));
                    map.put(formBody.encodedName(i),finalStr);
                }
                paramsSign = getParamsSign(map);

                FormBody.Builder newFormBody = new FormBody.Builder();
                for (Map.Entry<String, String> mapEntry : map.entrySet()) {
                    if (!TextUtils.isEmpty(mapEntry.getValue())) {
                        newFormBody.add(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
                newFormBody.add("sign", paramsSign);
                requestBuilder.method(original.method(), newFormBody.build());
                break;
            default:
        }
        Request request = requestBuilder.url(url).build();
        return chain.proceed(request);
    }

    private String encode(String origin) {
        try {
            return Coder.encrypt(origin, "1234fghjnmlkiuhA");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getParamsSign(Map<String, String> map){
        String paramsSign_ = "";
        for (Map.Entry<String, String> entry : map.entrySet()) {
            paramsSign_ += entry.getKey();
            paramsSign_ += entry.getValue();
        }

        return getMD5(paramsSign_);
    }

    public static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
        } catch (Exception e) {

        }
        return  null;
    }
}
