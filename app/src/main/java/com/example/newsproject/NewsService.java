package com.example.newsproject;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsService extends IntentService {

    static final String NEWS_API = "https://newsapi.org/v2/top-headlines?country=il&apiKey=41234c5fe5fe42729a16fcbc06850d84";
    static final String NEWS_SERVICE_TAG = "NEWS_SERVICE_TAG";
    public static final String ACTION = "com.example.newsproject";
    private static final int NEWS_REQ = 1;


    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {

        final ArrayList<NewsReport> newsList = new ArrayList<>();
        final RequestQueue queue = Volley.newRequestQueue(this);

        if(newsList != null) {
            // Start the json parsing
            final StringRequest request = new StringRequest(NEWS_API, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject rootObject = new JSONObject(response);
                        JSONArray articlesArray = rootObject.getJSONArray("articles");

                        String siteName = null;
                        String articleCDesc = null;
                        String date = null;
                        String title = null;
                        String imageUrl = null;
                        String articleUrl = null;

                        for(int i = 0; i < articlesArray.length(); i++) {

                            JSONObject article = (JSONObject) articlesArray.get(i);
                            JSONObject sourceObject = article.getJSONObject("source");
                            String siteUrl = sourceObject.getString("name");
                            int firstDotIndex = siteUrl.indexOf(".");

                            if(firstDotIndex != -1) siteName = siteUrl.substring(0, firstDotIndex).toLowerCase();
                            else siteName = siteUrl.toLowerCase();

                            articleCDesc = article.getString("description");
                            date = parseDate(article.getString("publishedAt"));
                            imageUrl = article.getString("urlToImage");
                            title = article.getString("title");
                            articleUrl = article.getString("url");

                            NewsReport newsReport = new NewsReport(title, articleCDesc, date,
                                                                    imageUrl, siteName, articleUrl);
                            newsList.add(newsReport);
                        }

                        Bundle bundle = new Bundle();
                        bundle.putBoolean("is_okay", true);
                        bundle.putSerializable("news_list", newsList);

                        Intent finishIntent = new Intent(ACTION);
                        finishIntent.putExtra("bundle", bundle);
                        LocalBroadcastManager.getInstance(NewsService.this).sendBroadcast(finishIntent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                private String parseDate(String publishedAt) {

                    int firstT = publishedAt.indexOf("T");
                    if(firstT != -1) return publishedAt.substring(0, firstT);

                    return publishedAt;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.d(NEWS_SERVICE_TAG, error.getMessage());
                }
            });

            queue.add(request);
            queue.start();
        }
    }


    public NewsService() {
        super("News Thread");
    }


}
