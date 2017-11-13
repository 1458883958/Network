package com.example.wudelin.network;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by wudelin on 2017/11/13.
 */

public class HttpUtil {

    public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection connection = null;
                        try {
                            URL url = new URL(address);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(8000);
                            connection.setReadTimeout(8000);
                            connection.setDoOutput(true);
                            connection.setDoInput(true);
                            InputStream in = connection.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while((line=reader.readLine())!=null){
                                response.append(line);
                            }
                            if (listener != null){
                                listener.onFinish(response.toString());
                            }

                        } catch (Exception e) {
                            if(listener!=null)
                                listener.onError(e);
                        }finally {
                            if(connection!=null) {
                                connection.disconnect();
                            }
                        }
                    }
                }
        ).start();
    }

    public static void sendOkHttpRequest(final String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
