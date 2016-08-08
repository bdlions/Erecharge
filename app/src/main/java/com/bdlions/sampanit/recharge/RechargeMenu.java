package com.bdlions.sampanit.recharge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.bdlions.sampanit.database.DatabaseHelper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RechargeMenu extends AppCompatActivity {
    private static String baseUrl = "";
    private static int userId = 0;
    private static int localUserId = 0;
    private static String sessionId = "";
    private static TextView userName, currentBalance;
    UserInfo userInfo = new UserInfo();
    private boolean topUpFlag = false;
    private  int[] serviceList;
    GridView grid;
    private String strUserInfo;
    List<Integer> history_services = new ArrayList<Integer>();
    private static DatabaseHelper eRchargeDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userName = (TextView)findViewById(R.id.userName);
        currentBalance = (TextView)findViewById(R.id.currentBalance);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        eRchargeDB = DatabaseHelper.getInstance(this);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            serviceList = getIntent().getExtras().getIntArray("service_list");
            genarateServiceOptions();
            baseUrl = getIntent().getExtras().getString("BASE_URL");
            currentBalance.setText(getIntent().getExtras().getString("CURRENT_BALANCE"));
            sessionId = getIntent().getExtras().getString("SESSION_ID");
            try
            {
                strUserInfo = getIntent().getExtras().getString("USER_INFO");
                JSONObject jsonUserInfo  = new JSONObject(strUserInfo);
                userInfo.setFirstName((String) jsonUserInfo.get("first_name"));
                userInfo.setLastName((String) jsonUserInfo.get("last_name"));
                userInfo.setUserId(Integer.parseInt((String) jsonUserInfo.get("user_id")));
                userId = Integer.parseInt((String) jsonUserInfo.get("user_id"));
                String UserName = userInfo.getFirstName() +" "+   userInfo.getLastName();
                userName.setText(UserName);
            }
            catch(Exception ex)
            {
                //handle the exception here
            }

       }else if(netInfo != null && netInfo.isConnected()){
            JSONObject localUserInfo =  eRchargeDB.getUserInfo();
            try {
                localUserId = (int) localUserInfo.get("userId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            getUserInfo();
        }else{
            JSONObject localUserInfo =  eRchargeDB.getUserInfo();
              serviceList = new int[]{};
            try {
                String localUserName= (String) localUserInfo.get("userName");
                userName.setText(localUserName);
                genarateServiceOptions();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }



    }



    public void getUserInfo(){
        try
        {
            final ProgressDialog progress = new ProgressDialog(RechargeMenu.this);
            progress.setTitle("Connecting");
            progress.setMessage("Connecting to server ...");
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
                        HttpPost post = new HttpPost(" http://122.144.10.249/rechargeserver/androidapp/transaction/get_user_basic_info");

                        List<NameValuePair> nameValuePairs = new ArrayList<>();
                        nameValuePairs.add(new BasicNameValuePair("user_id", localUserId+""));
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
                                    JSONObject jsonResultEvent = (JSONObject) resultEvent.get("result_event");

                                    currentBalance.setText(jsonResultEvent.get("current_balance").toString());
                                    sessionId = jsonResultEvent.get("session_id").toString();
                                    String tempUserInfo = jsonResultEvent.get("user_info").toString();
                                    JSONObject jsonUserInfo  = new JSONObject(tempUserInfo);
                                    userInfo.setFirstName((String) jsonUserInfo.get("first_name"));
                                    userInfo.setLastName((String) jsonUserInfo.get("last_name"));
                                    userInfo.setUserId(Integer.parseInt((String) jsonUserInfo.get("user_id")));
                                    userId = Integer.parseInt((String) jsonUserInfo.get("user_id"));
                                    String UserName = userInfo.getFirstName() +" "+   userInfo.getLastName();
                                    userName.setText(UserName);
                                    //getting service id list
                                    JSONArray serviceIdList = jsonResultEvent.getJSONArray("service_id_list");
                                    serviceList = new int[serviceIdList.length()];
                                    for (int i = 0; i < serviceIdList.length(); i++)
                                    {
                                        int serviceId = (int)serviceIdList.get(i);
                                        serviceList[i] = serviceId;
                                    }

                                    genarateServiceOptions();

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


    public void genarateServiceOptions(){


        List<String> grid_services = new ArrayList<String>();

        List<Integer> grid_image = new ArrayList<Integer>();


        for (int i = 0; i < serviceList.length; i++) {
            if(serviceList[i] == Constants.SERVICE_TYPE_ID_BKASH_CASHIN){
                grid_services.add("bKash");
                grid_image.add( R.drawable.bkash);
                history_services.add(Constants.SERVICE_TYPE_ID_BKASH_CASHIN);
            } else if(serviceList[i] == Constants.SERVICE_TYPE_ID_DBBL_CASHIN){
                grid_services.add("DBBL");
                grid_image.add( R.drawable.dbbl);
                history_services.add(Constants.SERVICE_TYPE_ID_DBBL_CASHIN);
            } else if(serviceList[i] == Constants.SERVICE_TYPE_ID_MCASH_CASHIN){
                grid_services.add("mCash");
                grid_image.add( R.drawable.mcash);
                history_services.add(Constants.SERVICE_TYPE_ID_MCASH_CASHIN);
            }  else if(serviceList[i] == Constants.SERVICE_TYPE_ID_UCASH_CASHIN){
                grid_services.add("UCash");
                grid_image.add( R.drawable.ucash);
                history_services.add(Constants.SERVICE_TYPE_ID_UCASH_CASHIN);
            }  else if(serviceList[i] == Constants.SERVICE_TYPE_ID_TOPUP_GP
                    || serviceList[i] == Constants.SERVICE_TYPE_ID_TOPUP_AIRTEL
                    ||  serviceList[i] == Constants.SERVICE_TYPE_ID_TOPUP_BANGLALINK
                    ||  serviceList[i] == Constants.SERVICE_TYPE_ID_TOPUP_ROBI
                    ||  serviceList[i] == Constants.SERVICE_TYPE_ID_TOPUP_TELETALK){
                topUpFlag = true;
            }
        }
        if(topUpFlag != false){
            grid_services.add("TopUp");
            grid_image.add( R.drawable.flexiload);
            history_services.add(Constants.SERVICE_TYPE_TOPUP_HISTORY_FLAG);

        }

        grid_services.add("History");
        grid_image.add( R.drawable.history);
        grid_services.add("Account");
        grid_image.add( R.drawable.account);
        CustomGrid adapter = new CustomGrid(RechargeMenu.this, grid_services, grid_image);
        grid=(GridView)findViewById(R.id.list);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        Intent intentbKash = new Intent(getBaseContext(), bKash.class);
                        intentbKash.putExtra("BASE_URL", baseUrl);
                        intentbKash.putExtra("USER_ID", userId);
                        intentbKash.putExtra("SESSION_ID", sessionId);
                        intentbKash.putExtra("USER_INFO", strUserInfo);
                        startActivityForResult(intentbKash, Constants.PAGE_BKASH);
                        break;

                    case 1:
                        Intent intentDBBL = new Intent(getBaseContext(), DBBL.class);
                        intentDBBL.putExtra("BASE_URL", baseUrl);
                        intentDBBL.putExtra("USER_ID", userId);
                        intentDBBL.putExtra("SESSION_ID", sessionId);
                        startActivityForResult(intentDBBL, Constants.PAGE_DBBL);
                        break;

                    case 2:
                        Intent intentmCash = new Intent(getBaseContext(), mCash.class);
                        intentmCash.putExtra("BASE_URL", baseUrl);
                        intentmCash.putExtra("USER_ID", userId);
                        intentmCash.putExtra("SESSION_ID", sessionId);
                        startActivityForResult(intentmCash, Constants.PAGE_MCASH);
                        break;
                    case 3:
                        Intent intentUCash = new Intent(getBaseContext(), UCash.class);
                        intentUCash.putExtra("BASE_URL", baseUrl);
                        intentUCash.putExtra("USER_ID", userId);
                        intentUCash.putExtra("SESSION_ID", sessionId);
                        startActivityForResult(intentUCash, Constants.PAGE_UCASH);
                        break;
                    case 4:
                        Intent intentTopUp = new Intent(getBaseContext(), TopUp.class);
                        intentTopUp.putExtra("BASE_URL", baseUrl);
                        intentTopUp.putExtra("USER_ID", userId);
                        intentTopUp.putExtra("SESSION_ID", sessionId);
                        startActivityForResult(intentTopUp, Constants.PAGE_TOPUP);
                        break;
                    case 5:
                        Intent intentHistory = new Intent(getBaseContext(), History.class);
                        intentHistory.putExtra("BASE_URL", baseUrl);
                        intentHistory.putExtra("USER_INFO", strUserInfo);
                        intentHistory.putIntegerArrayListExtra("history_services", (ArrayList<Integer>) history_services);
                        startActivity(intentHistory);
                        break;
                    case 6:
                        Intent intentAccount = new Intent(getBaseContext(), Account.class);
                        intentAccount.putExtra("BASE_URL", baseUrl);
                        intentAccount.putExtra("USER_INFO", strUserInfo);
                        intentAccount.putIntegerArrayListExtra("history_services", (ArrayList<Integer>) history_services);
                        startActivity(intentAccount);
                        break;
                }
            }
        });





    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PAGE_BKASH) {
            if(resultCode == Constants.PAGE_BKASH_BACK){
                //nothing to do
            }
            else if(resultCode == Constants.PAGE_BKASH_TRANSACTION_SUCCESS){
                currentBalance.setText(data.getStringExtra("currentBalance"));
                Toast.makeText(getApplicationContext(), "Transaction successful.", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_BKASH_SERVER_UNAVAILABLE){
                Toast.makeText(getApplicationContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_BKASH_SERVER_ERROR){
                Toast.makeText(getApplicationContext(), data.getStringExtra("message"), Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_BKASH_SESSION_EXPIRED){
                Toast.makeText(getApplicationContext(), "Your session is expired", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), Login.class);
                startActivity(intent);
            }
        }

        else if (requestCode == Constants.PAGE_DBBL) {
            if(resultCode == Constants.PAGE_DBBL_TRANSACTION_SUCCESS){
                currentBalance.setText(data.getStringExtra("currentBalance"));
                Toast.makeText(getApplicationContext(), "Transaction successful.", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_DBBL_SERVER_UNAVAILABLE){
                Toast.makeText(getApplicationContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_DBBL_SERVER_ERROR){
                Toast.makeText(getApplicationContext(), data.getStringExtra("message"), Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_DBBL_SESSION_EXPIRED){
                Toast.makeText(getApplicationContext(), "Your session is expired", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), Login.class);
                startActivity(intent);
            }
        }

        else if (requestCode == Constants.PAGE_MCASH) {
            if(resultCode == Constants.PAGE_MCASH_TRANSACTION_SUCCESS){
                currentBalance.setText(data.getStringExtra("currentBalance"));
                Toast.makeText(getApplicationContext(), "Transaction successful.", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_MCASH_SERVER_UNAVAILABLE){
                Toast.makeText(getApplicationContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_MCASH_SERVER_ERROR){
                Toast.makeText(getApplicationContext(), data.getStringExtra("message"), Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_MCASH_SESSION_EXPIRED){
                Toast.makeText(getApplicationContext(), "Your session is expired", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), Login.class);
                startActivity(intent);
            }
        }

        else if (requestCode == Constants.PAGE_UCASH) {
            if(resultCode == Constants.PAGE_UCASH_TRANSACTION_SUCCESS){
                currentBalance.setText(data.getStringExtra("currentBalance"));
                Toast.makeText(getApplicationContext(), "Transaction successful.", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_UCASH_SERVER_UNAVAILABLE){
                Toast.makeText(getApplicationContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_UCASH_SERVER_ERROR){
                Toast.makeText(getApplicationContext(), data.getStringExtra("message"), Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_UCASH_SESSION_EXPIRED){
                Toast.makeText(getApplicationContext(), "Your session is expired", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), Login.class);
                startActivity(intent);
            }
        }

        else if (requestCode == Constants.PAGE_TOPUP) {
            if(resultCode == Constants.PAGE_TOPUP_TRANSACTION_SUCCESS){
                currentBalance.setText(data.getStringExtra("currentBalance"));
                Toast.makeText(getApplicationContext(), "Transaction successful.", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_TOPUP_SERVER_UNAVAILABLE){
                Toast.makeText(getApplicationContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_TOPUP_SERVER_ERROR){
                Toast.makeText(getApplicationContext(), data.getStringExtra("message"), Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == Constants.PAGE_TOPUP_SESSION_EXPIRED){
                Toast.makeText(getApplicationContext(), "Your session is expired", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), Login.class);
                startActivity(intent);
            }
        }
    }


}
