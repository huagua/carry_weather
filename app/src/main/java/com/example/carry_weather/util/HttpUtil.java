package com.example.carry_weather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
import okhttp3.OkHttpClient;
import okhttp3.Request;

 */

public class HttpUtil {

    /*
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback)
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
     */

    public static StringBuilder sendRequestWithHttpUrl(final String address )
    {
        final StringBuilder response = new StringBuilder();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;

                try{
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader((in)));

                    String line;
                    while((line = reader.readLine())!=null)
                    {
                        response.append(line);
                    }

                }catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(reader != null)
                    {
                        try{
                            reader.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();

        return response;
    }



}
