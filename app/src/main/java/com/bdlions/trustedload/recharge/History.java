package com.bdlions.trustedload.recharge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.bdlions.trustedload.database.DatabaseHelper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {
    private static DatabaseHelper databaseHelper;
    private static String baseUrl = "";
    private static int userId = 0;
    private static String sessionId;

    ArrayList<Integer> history_services ;
    GridView grid_history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        List<Integer> grid_image = new ArrayList<Integer>();
        List<String> grid_text_list = new ArrayList<String>();

        databaseHelper = DatabaseHelper.getInstance(this);
        try
        {
            JSONObject localUserInfo =  databaseHelper.getUserInfo();
            userId = (int) localUserInfo.get("userId");
            baseUrl = (String) localUserInfo.get("baseUrl");
            sessionId = (String) localUserInfo.get("sessionId");
            if(userId < 0)
            {
                Toast.makeText(getApplicationContext(), "Invalid user.", Toast.LENGTH_SHORT).show();
                databaseHelper.deleteUserInfo();
                Intent intentLogin = new Intent(getBaseContext(), Login.class);
                startActivity(intentLogin);
            }
        }
        catch(Exception ex)
        {
            Toast.makeText(getApplicationContext(), "Please login again. Error:"+ex.toString(), Toast.LENGTH_SHORT).show();
            databaseHelper.deleteUserInfo();
            Intent intentLogin = new Intent(getBaseContext(), Login.class);
            startActivity(intentLogin);
            return;
        }

        history_services = getIntent().getExtras().getIntegerArrayList("history_services");
        for (int i = 0; i < history_services.size(); i++) {
            if(history_services.get(i) == Constants.SERVICE_TYPE_TOPUP_HISTORY_FLAG){
                grid_image.add(R.drawable.flexiload);
                grid_text_list.add(Constants.SERVICE_TYPE_TITLE_TOPUP_HISTORY);

            }
            else if(history_services.get(i) == Constants.SERVICE_TYPE_ID_BKASH_CASHIN){
                grid_image.add( R.drawable.bkash);
                grid_text_list.add(Constants.SERVICE_TYPE_TITLE_BKASH_HISTORY);
            }
            else if(history_services.get(i) == Constants.SERVICE_TYPE_ID_DBBL_CASHIN){
                grid_image.add( R.drawable.dbbl);
                grid_text_list.add(Constants.SERVICE_TYPE_TITLE_DBBL_HISTORY);
            }
            else if(history_services.get(i) == Constants.SERVICE_TYPE_ID_MCASH_CASHIN){
                grid_image.add( R.drawable.mcash);
                grid_text_list.add(Constants.SERVICE_TYPE_TITLE_MCASH_HISTORY);
            }
            else if(history_services.get(i) == Constants.SERVICE_TYPE_ID_UCASH_CASHIN){
                grid_image.add( R.drawable.ucash);
                grid_text_list.add(Constants.SERVICE_TYPE_TITLE_UCASH_HISTORY);
            }
        }

        final CustomGrid historyAdapter = new CustomGrid(History.this, grid_text_list, grid_image);
        grid_history=(GridView)findViewById(R.id.history_list);
        grid_history.setAdapter(historyAdapter);
        grid_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String serviceName =  historyAdapter.getItemName(position);
                switch (serviceName) {
                    case Constants.SERVICE_TYPE_TITLE_BKASH_HISTORY:

                        showBkashHistory();
                        break;

                    case Constants.SERVICE_TYPE_TITLE_DBBL_HISTORY:
                        showDBBLHistory();
                        break;

                    case Constants.SERVICE_TYPE_TITLE_MCASH_HISTORY:
                        showMcashHistory();
                        break;

                    case Constants.SERVICE_TYPE_TITLE_UCASH_HISTORY:
                        showUcashHistory();
                        break;

                    case Constants.SERVICE_TYPE_TITLE_TOPUP_HISTORY:
                        showTopupHistory();
                        break;

                }
            }
        });
    }


    public void showBkashHistory()
    {
        try
        {
            final ProgressDialog progress = new ProgressDialog(History.this);
            progress.setTitle("Processing");
            progress.setMessage("Retrieving Bkash transaction list...");
            progress.show();
            Thread bkashThread = new Thread() {
                @Override
                public void run()
                {
                    try
                    {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(baseUrl + Constants.URL_TRANSACTION_LIST_BKASH);

                        List<NameValuePair> nameValuePairs = new ArrayList<>();
                        nameValuePairs.add(new BasicNameValuePair("user_id", userId+""));
                        nameValuePairs.add(new BasicNameValuePair("session_id", sessionId));

                        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = client.execute(post);
                        BufferedReader rd = new BufferedReader
                                (new InputStreamReader(response.getEntity().getContent()));
                        String result = "";
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            result += line;
                        }
                        if(result != null) {
                            JSONObject resultEvent = new JSONObject(result.toString());

                            int responseCode = 0;
                            try
                            {
                                responseCode = (int)resultEvent.get("response_code");
                            }
                            catch(Exception ex)
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Invalid response from the server.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(responseCode == 2000){
                                try
                                {
                                    JSONArray transactionList = resultEvent.getJSONArray("transaction_list");
                                    Intent intentbKash = new Intent(getBaseContext(), BKashHistory.class);
                                    intentbKash.putExtra("TRANSACTION_LIST", transactionList.toString());
                                    startActivity(intentbKash);
                                }
                                catch(Exception ex)
                                {
                                    progress.dismiss();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getBaseContext(), "Invalid response from the server..", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            }
                            else if(responseCode == 5001)
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Your session is expired. Please login again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Empty response from the server.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        else
                        {
                            progress.dismiss();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Invalid response from the server...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    catch (Exception ex) {
                        progress.dismiss();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    progress.dismiss();
                }
            };
            bkashThread.start();
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(), "Internal server error.", Toast.LENGTH_SHORT).show();
        }
    }

    public void showDBBLHistory()
    {
        try
        {
            final ProgressDialog progress = new ProgressDialog(History.this);
            progress.setTitle("Processing");
            progress.setMessage("Retrieving DBBL transaction list...");
            progress.show();
            Thread bkashThread = new Thread() {
                @Override
                public void run()
                {
                    try
                    {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(baseUrl + Constants.URL_TRANSACTION_LIST_DBBL);

                        List<NameValuePair> nameValuePairs = new ArrayList<>();
                        nameValuePairs.add(new BasicNameValuePair("user_id", userId+""));
                        nameValuePairs.add(new BasicNameValuePair("session_id", sessionId));

                        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = client.execute(post);
                        BufferedReader rd = new BufferedReader
                                (new InputStreamReader(response.getEntity().getContent()));
                        String result = "";
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            result += line;
                        }
                        if(result != null) {
                            JSONObject resultEvent = new JSONObject(result.toString());

                            int responseCode = 0;
                            try
                            {
                                responseCode = (int)resultEvent.get("response_code");
                            }
                            catch(Exception ex)
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Invalid response from the server.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(responseCode == 2000){
                                try
                                {
                                    JSONArray transactionList = resultEvent.getJSONArray("transaction_list");
                                    Intent intentDBBL = new Intent(getBaseContext(), DBBLHistory.class);
                                    intentDBBL.putExtra("TRANSACTION_LIST", transactionList.toString());
                                    startActivity(intentDBBL);
                                }
                                catch(Exception ex)
                                {
                                    progress.dismiss();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getBaseContext(), "Invalid response from the server..", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            }
                            else if(responseCode == 5001)
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Your session is expired. Please login again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Empty response from the server.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        else
                        {
                            progress.dismiss();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Invalid response from the server...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    catch (Exception ex) {
                        progress.dismiss();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    progress.dismiss();
                }
            };
            bkashThread.start();
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(), "Internal server error.", Toast.LENGTH_SHORT).show();
        }
    }

    public void showMcashHistory()
    {
        try
        {
            final ProgressDialog progress = new ProgressDialog(History.this);
            progress.setTitle("Processing");
            progress.setMessage("Retrieving Mcash transaction list...");
            progress.show();
            Thread bkashThread = new Thread() {
                @Override
                public void run()
                {
                    try
                    {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(baseUrl + Constants.URL_TRANSACTION_LIST_MCASH);

                        List<NameValuePair> nameValuePairs = new ArrayList<>();
                        nameValuePairs.add(new BasicNameValuePair("user_id", userId+""));
                        nameValuePairs.add(new BasicNameValuePair("session_id", sessionId));

                        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = client.execute(post);
                        BufferedReader rd = new BufferedReader
                                (new InputStreamReader(response.getEntity().getContent()));
                        String result = "";
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            result += line;
                        }
                        if(result != null) {
                            JSONObject resultEvent = new JSONObject(result.toString());

                            int responseCode = 0;
                            try
                            {
                                responseCode = (int)resultEvent.get("response_code");
                            }
                            catch(Exception ex)
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Invalid response from the server.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(responseCode == 2000){
                                try
                                {
                                    JSONArray transactionList = resultEvent.getJSONArray("transaction_list");
                                    Intent intentmCash = new Intent(getBaseContext(), MCashHistory.class);
                                    intentmCash.putExtra("TRANSACTION_LIST", transactionList.toString());
                                    startActivity(intentmCash);
                                }
                                catch(Exception ex)
                                {
                                    progress.dismiss();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getBaseContext(), "Invalid response from the server..", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            }
                            else if(responseCode == 5001)
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Your session is expired. Please login again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Empty response from the server.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        else
                        {
                            progress.dismiss();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Invalid response from the server...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    catch (Exception ex) {
                        progress.dismiss();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    progress.dismiss();
                }
            };
            bkashThread.start();
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(), "Internal server error.", Toast.LENGTH_SHORT).show();
        }
    }

    public void showUcashHistory()
    {
        try
        {
            final ProgressDialog progress = new ProgressDialog(History.this);
            progress.setTitle("Processing");
            progress.setMessage("Retrieving Ucash transaction list...");
            progress.show();
            Thread bkashThread = new Thread() {
                @Override
                public void run()
                {
                    try
                    {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(baseUrl + Constants.URL_TRANSACTION_LIST_UCASH);

                        List<NameValuePair> nameValuePairs = new ArrayList<>();
                        nameValuePairs.add(new BasicNameValuePair("user_id", userId+""));
                        nameValuePairs.add(new BasicNameValuePair("session_id", sessionId));

                        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = client.execute(post);
                        BufferedReader rd = new BufferedReader
                                (new InputStreamReader(response.getEntity().getContent()));
                        String result = "";
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            result += line;
                        }
                        if(result != null) {
                            JSONObject resultEvent = new JSONObject(result.toString());

                            int responseCode = 0;
                            try
                            {
                                responseCode = (int)resultEvent.get("response_code");
                            }
                            catch(Exception ex)
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Invalid response from the server.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(responseCode == 2000){
                                try
                                {
                                    JSONArray transactionList = resultEvent.getJSONArray("transaction_list");
                                    Intent intentUCash = new Intent(getBaseContext(), UCashHistory.class);
                                    intentUCash.putExtra("TRANSACTION_LIST", transactionList.toString());
                                    startActivity(intentUCash);
                                }
                                catch(Exception ex)
                                {
                                    progress.dismiss();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getBaseContext(), "Invalid response from the server..", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            }
                            else if(responseCode == 5001)
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Your session is expired. Please login again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Empty response from the server.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        else
                        {
                            progress.dismiss();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Invalid response from the server...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    catch (Exception ex) {
                        progress.dismiss();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    progress.dismiss();
                }
            };
            bkashThread.start();
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(), "Internal server error.", Toast.LENGTH_SHORT).show();
        }
    }

    public void showTopupHistory()
    {
        try
        {
            final ProgressDialog progress = new ProgressDialog(History.this);
            progress.setTitle("Processing");
            progress.setMessage("Retrieving Topup transaction list...");
            progress.show();
            Thread bkashThread = new Thread() {
                @Override
                public void run()
                {
                    try
                    {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(baseUrl + Constants.URL_TRANSACTION_LIST_TOPUP);

                        List<NameValuePair> nameValuePairs = new ArrayList<>();
                        nameValuePairs.add(new BasicNameValuePair("user_id", userId+""));
                        nameValuePairs.add(new BasicNameValuePair("session_id", sessionId));

                        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = client.execute(post);
                        BufferedReader rd = new BufferedReader
                                (new InputStreamReader(response.getEntity().getContent()));
                        String result = "";
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            result += line;
                        }
                        if(result != null) {
                            JSONObject resultEvent = new JSONObject(result.toString());

                            int responseCode = 0;
                            try
                            {
                                responseCode = (int)resultEvent.get("response_code");
                            }
                            catch(Exception ex)
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Invalid response from the server.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(responseCode == 2000){
                                try
                                {
                                    JSONArray transactionList = resultEvent.getJSONArray("transaction_list");
                                    Intent intentTopUp = new Intent(getBaseContext(), TopUpHistory.class);
                                    intentTopUp.putExtra("TRANSACTION_LIST", transactionList.toString());
                                    startActivity(intentTopUp);
                                }
                                catch(Exception ex)
                                {
                                    progress.dismiss();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getBaseContext(), "Invalid response from the server..", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            }
                            else if(responseCode == 5001)
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Your session is expired. Please login again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else
                            {
                                progress.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Empty response from the server.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        else
                        {
                            progress.dismiss();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Invalid response from the server...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    catch (Exception ex) {
                        progress.dismiss();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    progress.dismiss();
                }
            };
            bkashThread.start();
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(), "Internal server error.", Toast.LENGTH_SHORT).show();
        }
    }
}
