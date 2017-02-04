package com.retrofitstudy.mock;

import android.util.Log;

import com.google.gson.JsonObject;
import com.retrofitstudy.common.http.RetrofitUtil;

import fi.iki.elonen.NanoHTTPD;

public class MockServer extends NanoHTTPD {

    public MockServer() {
        super(9876);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Log.d(RetrofitUtil.TAG, "url:" + uri);
        Log.d(RetrofitUtil.TAG, "url:" + session.getParameters().toString());
        if ("/request".equals(uri)) {
            return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, makeResponseContent());
        }
        return super.serve(session);
    }

    private String makeResponseContent() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error_code", 0);
        jsonObject.addProperty("error_desc", "成功");
        JsonObject subJsonObject = new JsonObject();
        subJsonObject.addProperty("response_server", "来自Mock服务器的问候");
        jsonObject.add("data", subJsonObject);
        return jsonObject.toString();
    }
}
