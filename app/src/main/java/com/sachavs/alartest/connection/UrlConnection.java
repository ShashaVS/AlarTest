package com.sachavs.alartest.connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlConnection {
    private final String TAG = "Connection";

    public String requestGet(URL url) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("GET");

            int response = urlConnection.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream in = urlConnection.getInputStream();
                return readStream(in);
            }
        } catch (Exception e) {
            Log.e(TAG, "requestGet Exception: ", e);
        } finally {
            if(urlConnection != null) urlConnection.disconnect();
        }
        return null;
    }

    public Bitmap loadImage(URL url) {
        Bitmap bitmap = null;
        InputStream in = null;
        try {
            in = url.openStream();
            bitmap = BitmapFactory.decodeStream(in);

        } catch (Exception e) {
            Log.e(TAG, "loadImage Exception: ", e);
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "loadImage close InputStream Exception: ", e);
                }
            }
        }
        return bitmap;
    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String line;
        StringBuilder stringBuilder = new StringBuilder();

        while((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();

        return stringBuilder.toString();
    }

}
