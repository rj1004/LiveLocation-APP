package com.example.livelocation;

import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        System.out.println( "dfdsf");
    }
    @Test
    public void getUpdateLocation() {
        OkHttpClient client=new OkHttpClient();
        Request r=new Request.Builder()
                .url("http://192.168.43.159:9898/see/123")
                .get()
                .build();
        System.out.println("hello");

        client.newCall(r).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                System.out.println( e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String body = response.body().string();
                System.out.println( body);
            }
        });

    }
}