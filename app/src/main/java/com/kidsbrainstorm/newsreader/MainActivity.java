package com.kidsbrainstorm.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    ArrayList<Details_of_individual> arrayList;
    ArrayAdapter<Details_of_individual> arrayAdapter;

    int id = 0;
    SQLiteDatabase sqLiteDatabase;


    public void updateContent() {
        try{
            Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM articles", null);
            int titleIndex = c.getColumnIndex("title");
            int contentIndex = c.getColumnIndex("htmlContent");
            c.moveToFirst();
            while (c != null) {
                arrayList.add(new Details_of_individual(c.getString(contentIndex), c.getString(titleIndex)));
                c.moveToNext();
            }
            arrayAdapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder topIds = new StringBuilder();
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();
                while (data != -1) {
                    char c = (char) data;
                    topIds.append(c);
                    data = inputStreamReader.read();
                }
                Log.i("Line",topIds.toString());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                    sqLiteDatabase.execSQL("DELETE FROM articles");
                    JSONArray jsonArray = new JSONArray(topIds.toString());
                    int numberOfItems = 20;
                    StringBuilder articleContent, htmlContent;
                    JSONObject jsonObject=null;
                    String titleArticle;
                    String sql = "INSERT INTO articles(id,title, htmlContent) VALUES (?,?,?)";

                    Log.i("Line","88");
                    for (int i = 0; i < numberOfItems; i++) {

                        try{
                            id = Integer.parseInt(jsonArray.get(i).toString());
                        String url_details = "https://hacker-news.firebaseio.com/v0/item/" + id + ".json?print=pretty";
                        articleContent = new StringBuilder();
                        url = new URL(url_details);


                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.connect();
                            inputStream = urlConnection.getInputStream();

                        inputStreamReader = new InputStreamReader(inputStream);
                        data = inputStreamReader.read();
                        while (data != -1) {
                            char c = (char) data;
                            articleContent.append(c);
                            data = inputStreamReader.read();
                        }
                        Log.i("dsgsdfs",articleContent.toString());
                        }catch (FileNotFoundException fileNotFoundException){
                            continue;
                        }
                        try{
                        jsonObject = new JSONObject(articleContent.toString());
                        String urlArticle = jsonObject.getString("url");
                        titleArticle = jsonObject.getString("title");


                        Log.i("Line",urlArticle);
                        htmlContent = new StringBuilder();
                        url = new URL(urlArticle);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.connect();
                        inputStream = urlConnection.getInputStream();
                        inputStreamReader = new InputStreamReader(inputStream);
                        data = inputStreamReader.read();
                        while (data != -1) {
                            char c = (char) data;
                            htmlContent.append(c);
                            data = inputStreamReader.read();
                        }}catch (FileNotFoundException fileNotFoundException){
                            continue;
                        }

                        SQLiteStatement statement = sqLiteDatabase.compileStatement(sql);
                        statement.bindString(1, String.valueOf(id));
                        statement.bindString(2, titleArticle);
                        statement.bindString(3, htmlContent.toString());
                        statement.execute();
                        Log.i("sQl","successful");


                    }


                }
                return null;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateContent();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqLiteDatabase = getApplicationContext().openOrCreateDatabase("ArticlesDb", MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER, title VARCHAR, htmlContent VARCHAR)");
        arrayList = new ArrayList<>();
        updateContent();

        ListView listView = (ListView) findViewById(R.id.listView);

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);

        // Code to get Current Maximum id //
        try {
            DownloadTask downloadTask = new DownloadTask();
           downloadTask.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (Exception e) {
            Log.i("Line", "88");
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), webViewActivity2.class);
                intent.putExtra("data",arrayList.get(position).getUrl());
                startActivity(intent);
            }
        });
    }
}