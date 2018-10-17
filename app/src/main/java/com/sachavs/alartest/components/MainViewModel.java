package com.sachavs.alartest.components;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.sachavs.alartest.connection.UrlConnection;
import com.sachavs.alartest.fragments.objects.Item;
import com.sachavs.alartest.fragments.objects.Page;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private static String TAG = "MainViewModel";

    private UrlConnection urlConnection;
    private LruCache<String, Bitmap> lruCache;

    public MutableLiveData<String> authCode;
    public MutableLiveData<String> totalPages;
    private List<Item> items;
    private boolean isLoadingData;
    private String baseURL = "YOUR_BASE_URL";

    public MainViewModel(Application app) {
        super(app);
        urlConnection = new UrlConnection();

        int cacheSize = 4 * 1024 * 1024;
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };

        authCode = new MutableLiveData<>();
        totalPages = new MutableLiveData<>();
        items = new ArrayList<>();
    }

    public List<Item> getItems() {
        return items;
    }

    public boolean isLoading() {
        return isLoadingData;
    }

    public void authAsync(String username, String password) {
        new AuthAsyncTask(this).execute(username, password);
    }

    public void getListAsync(String code, String page) {
        if(!isLoadingData) {
            isLoadingData = true;
            new FetchDataAsyncTask(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, code, page);
        }
    }

    public void loadImageAsync(ImageView view, String id, String url) {
        Bitmap bitmap = lruCache.get(id);
        if(bitmap != null) {
            view.setImageBitmap(bitmap);
        } else {
            new LoadImageAsyncTask(this, view, id)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        }
    }

    private JSONObject auth(String username, String password)
            throws MalformedURLException, JSONException {

        URL url = new URL(baseURL + "/auth.cgi"
                +"?username=" + username
                +"&password=" + password
        );
        String response = urlConnection.requestGet(url);
        if(response != null) {
            return new JSONObject(response);
        }
        return null;
    }

    private JSONObject getListData(String code, String page) {
        try {
            URL url = new URL(baseURL + "/data.cgi"
                    +"?code=" + code
                    +"&p=" + page
            );
            String response = urlConnection.requestGet(url);
            if(response != null) {
                return new JSONObject(response);
            }
        } catch (Exception e) {
            Log.e(TAG, "getListData Exception: ", e);
        }
        return null;
    }

    @Override
    protected void onCleared() {
        lruCache.evictAll();
        lruCache = null;
        super.onCleared();
    }

    private static class AuthAsyncTask extends AsyncTask<String, Void, String> {

        private WeakReference<MainViewModel> reference;

        AuthAsyncTask(MainViewModel model) {
            reference = new WeakReference<>(model);
        }

        @Override
        protected String doInBackground(String... strings) {
            MainViewModel model = reference.get();

            if(model != null) {
                String username = strings[0];
                String password = strings[1];
                try {
                    JSONObject jsonObject = model.auth(username, password);
                    if(jsonObject != null) {
                        String status = jsonObject.getString("status");
                        if(status.equals("ok")) {
                            return jsonObject.getString("code");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "AuthAsyncTask Exception: ", e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String code) {
            MainViewModel model = reference.get();
            if(model != null) {
                model.authCode.postValue(code);
            }
            reference.clear();
            reference = null;
        }
    }

    private static class FetchDataAsyncTask extends AsyncTask<String, Void, Page> {

        private WeakReference<MainViewModel> reference;

        FetchDataAsyncTask(MainViewModel model) {
            reference = new WeakReference<>(model);
        }

        @Override
        protected Page doInBackground(String... strings) {
            MainViewModel model = reference.get();

            if(model != null) {
                String code = strings[0];
                String page = strings[1];
                try {
                    JSONObject jsonObject = model.getListData(code, page);
                    if(jsonObject != null) {
                        String status = jsonObject.getString("status");
                        if(status.equals("ok")) {
                            return parseResponse(jsonObject);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "FetchDataAsyncTask Exception: ", e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Page page) {
            MainViewModel model = reference.get();
            if(model != null) {
                model.isLoadingData = false;
                if(page != null) {
                    model.items.addAll(page.getData());
                    model.totalPages.postValue(page.getPage());
                } else {
                    model.totalPages.postValue("-1");
                }
            }
            reference.clear();
            reference = null;
        }

        private Page parseResponse(JSONObject jsonPage) {
            Page page = new Page();
            try {
                String strPage = jsonPage.getString("page");
                JSONArray jsonArray = jsonPage.getJSONArray("data");

                if(jsonArray.length() > 0) {
                    List<Item> data = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonData = jsonArray.getJSONObject(i);
                        String id = jsonData.getString("id");
                        String name = jsonData.getString("name");
                        String country = jsonData.getString("country");
                        String lat = jsonData.getString("lat");
                        String lon = jsonData.getString("lon");

                        Item item = new Item();
                        item.setId(id);
                        item.setName(name);
                        item.setCountry(country);
                        item.setLat(lat);
                        item.setLon(lon);

                        data.add(item);
                    }
                    page.setData(data);
                }
                page.setPage(strPage);

            } catch (JSONException e) {
                Log.e(TAG, "parseResponse Exception: ", e);
            }
            return page;
        }
    }

    private static class LoadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private WeakReference<MainViewModel> modelReference;
        private WeakReference<ImageView> viewReference;
        private String id;

        LoadImageAsyncTask(MainViewModel model, ImageView view, String id) {
            modelReference = new WeakReference<>(model);
            viewReference = new WeakReference<>(view);
            this.id = id;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            MainViewModel model = modelReference.get();

            if(model != null) {
                String strUrl = strings[0];
                try {
                    URL url = new URL(strUrl);
                    return model.urlConnection.loadImage(url);

                } catch (MalformedURLException e) {
                    Log.e(TAG, "LoadImageAsyncTask MalformedURLException: ", e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null) {
                MainViewModel model = modelReference.get();
                ImageView view = viewReference.get();

                if(model != null && model.lruCache != null) {
                    model.lruCache.put(id, bitmap);
                }
                if(view != null) {
                    view.setImageBitmap(bitmap);
                }
            }
            viewReference.clear();
            modelReference.clear();
            viewReference = null;
            modelReference = null;
        }
    }

}
