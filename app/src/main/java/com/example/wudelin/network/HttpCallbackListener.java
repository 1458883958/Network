package com.example.wudelin.network;

/**
 * Created by wudelin on 2017/11/13.
 */

public interface HttpCallbackListener {

    public void onFinish(String response);

    public void onError(Exception e);
}
