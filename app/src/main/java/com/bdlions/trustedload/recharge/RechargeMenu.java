package com.bdlions.trustedload.recharge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

public class RechargeMenu extends AppCompatActivity {
    private ConnectivityManager cm;
    private NetworkInfo netInfo;
    private static String message = "";

    private static String baseUrl = "";
    private static int userId = 0;
    private static String sessionId = "";
    public static TextView userName, currentBalance, companyName;
    private boolean topUpFlag = false;
    List<Integer> serviceList = new ArrayList<>();
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

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = cm.getActiveNetworkInfo();

        companyName = (TextView)findViewById(R.id.companyName);
        userName = (TextView)findViewById(R.id.userName);
        currentBalance = (TextView)findViewById(R.id.currentBalance);

        eRchargeDB = DatabaseHelper.getInstance(this);
        try {
            JSONObject localUserInfo =  eRchargeDB.getUserInfo();
            userId = (int) localUserInfo.get("userId");
            baseUrl = (String) localUserInfo.get("baseUrl");
            sessionId = (String) localUserInfo.get("sessionId");
            companyName.setText((String) localUserInfo.get("companyName"));
            userName.setText((String) localUserInfo.get("userName"));
            currentBalance.setText((double) localUserInfo.get("balance") + "");
            serviceList = eRchargeDB.getAllServices();
            genarateServiceOptions();
        }
        catch (Exception ex) {
            Toast.makeText(getBaseContext(), "System error:"+ex.toString(), Toast.LENGTH_SHORT).show();
            eRchargeDB.deleteUserInfo();
            Intent intentLogin = new Intent(getBaseContext(), Login.class);
            startActivity(intentLogin);
            return;
        }
    }
    public void genarateServiceOptions(){
        List<String> grid_services = new ArrayList<String>();
        List<Integer> grid_image = new ArrayList<Integer>();
        for (int i = 0; i < serviceList.size(); i++) {
            if(serviceList.get(i) == Constants.SERVICE_TYPE_ID_BKASH_CASHIN){
                grid_services.add(Constants.SERVICE_TYPE_TITLE_BKASH_CASHIN);
                grid_image.add( R.drawable.bkash);
                history_services.add(Constants.SERVICE_TYPE_ID_BKASH_CASHIN);
           } else if(serviceList.get(i) == Constants.SERVICE_TYPE_ID_DBBL_CASHIN){
                grid_services.add(Constants.SERVICE_TYPE_TITLE_DBBL_CASHIN);
                grid_image.add( R.drawable.dbbl);
                history_services.add(Constants.SERVICE_TYPE_ID_DBBL_CASHIN);
            } else if(serviceList.get(i) == Constants.SERVICE_TYPE_ID_MCASH_CASHIN){
                grid_services.add(Constants.SERVICE_TYPE_TITLE_MCASH_CASHIN);
                grid_image.add( R.drawable.mcash);
                history_services.add(Constants.SERVICE_TYPE_ID_MCASH_CASHIN);
            }  else if(serviceList.get(i) == Constants.SERVICE_TYPE_ID_UCASH_CASHIN){
                grid_services.add(Constants.SERVICE_TYPE_TITLE_UCASH_CASHIN);
                grid_image.add( R.drawable.ucash);
                history_services.add(Constants.SERVICE_TYPE_ID_UCASH_CASHIN);
            }  else if(serviceList.get(i) == Constants.SERVICE_TYPE_ID_TOPUP_GP
                    || serviceList.get(i) == Constants.SERVICE_TYPE_ID_TOPUP_AIRTEL
                    ||  serviceList.get(i) == Constants.SERVICE_TYPE_ID_TOPUP_BANGLALINK
                    ||  serviceList.get(i) == Constants.SERVICE_TYPE_ID_TOPUP_ROBI
                    ||  serviceList.get(i) == Constants.SERVICE_TYPE_ID_TOPUP_TELETALK){
                topUpFlag = true;
            }
        }
        if(topUpFlag != false){
            grid_services.add(Constants.SERVICE_TYPE_TITLE_TOPUP);
            grid_image.add( R.drawable.flexiload);
            history_services.add(Constants.SERVICE_TYPE_TOPUP_HISTORY_FLAG);
            grid_services.add(Constants.SERVICE_TYPE_TITLE_TOPUP_PACKAGE);
            grid_image.add( R.drawable.flexiload);
        }

        grid_services.add(Constants.TRANSACTION_HISTROY_TITLE);
        grid_image.add( R.drawable.history);
        grid_services.add(Constants.ACCOUNT_TITLE);
        grid_image.add(R.drawable.account);
        grid_services.add(Constants.TITLE_LOGOUT);
        grid_image.add( R.drawable.logout);
        final CustomGrid adapter = new CustomGrid(RechargeMenu.this, grid_services, grid_image);
        grid=(GridView)findViewById(R.id.list);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               String serviceName =  adapter.getItemName(position);
                switch (serviceName) {
                    case Constants.SERVICE_TYPE_TITLE_BKASH_CASHIN:
                        Intent intentbKash = new Intent(getBaseContext(), BKash.class);
                        startActivity(intentbKash);
                        break;

                    case Constants.SERVICE_TYPE_TITLE_DBBL_CASHIN:
                        Intent intentDBBL = new Intent(getBaseContext(), DBBL.class);
                        intentDBBL.putExtra("BASE_URL", baseUrl);
                        intentDBBL.putExtra("USER_ID", userId);
                        intentDBBL.putExtra("SESSION_ID", sessionId);
                        startActivityForResult(intentDBBL, Constants.PAGE_DBBL);
                        break;

                    case Constants.SERVICE_TYPE_TITLE_MCASH_CASHIN:
                        Intent intentmCash = new Intent(getBaseContext(), MCash.class);
                        intentmCash.putExtra("BASE_URL", baseUrl);
                        intentmCash.putExtra("USER_ID", userId);
                        intentmCash.putExtra("SESSION_ID", sessionId);
                        startActivityForResult(intentmCash, Constants.PAGE_MCASH);
                        break;
                    case Constants.SERVICE_TYPE_TITLE_UCASH_CASHIN:
                        Intent intentUCash = new Intent(getBaseContext(), UCash.class);
                        intentUCash.putExtra("BASE_URL", baseUrl);
                        intentUCash.putExtra("USER_ID", userId);
                        intentUCash.putExtra("SESSION_ID", sessionId);
                        startActivityForResult(intentUCash, Constants.PAGE_UCASH);
                        break;
                    case Constants.SERVICE_TYPE_TITLE_TOPUP:
                        Intent intentTopUp = new Intent(getBaseContext(), TopUp.class);
                        startActivity(intentTopUp);
                        break;
                    case Constants.SERVICE_TYPE_TITLE_TOPUP_PACKAGE:
                        managePackageRecharge();
                        break;
                    case Constants.TRANSACTION_HISTROY_TITLE:
                        Intent intentHistory = new Intent(getBaseContext(), History.class);
                        intentHistory.putIntegerArrayListExtra("history_services", (ArrayList<Integer>) history_services);
                        startActivity(intentHistory);
                        break;
                    case Constants.ACCOUNT_TITLE:
                        Intent intentAccount = new Intent(getBaseContext(), Account.class);
                        startActivity(intentAccount);
                        break;
                    case Constants.TITLE_LOGOUT:
                        if(userId > 0)
                        {
                            try
                            {
                                cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                netInfo = cm.getActiveNetworkInfo();
                                if(netInfo == null || !netInfo.isConnected())
                                {
                                    Toast.makeText(getApplicationContext(), "Please connect to internet first.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                final ProgressDialog progressInit = new ProgressDialog(RechargeMenu.this);
                                progressInit.setTitle("Logout");
                                progressInit.setMessage("Logging out...");
                                progressInit.show();
                                final Thread logoutThread = new Thread() {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                            StrictMode.setThreadPolicy(policy);
                                            HttpClient client = new DefaultHttpClient();
                                            HttpPost post = new HttpPost(baseUrl+Constants.URL_LOGOUT);

                                            List<NameValuePair> nameValuePairs = new ArrayList<>();
                                            nameValuePairs.add(new BasicNameValuePair("user_id", "" + userId));
                                            nameValuePairs.add(new BasicNameValuePair("session_id", "" + sessionId));

                                            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                                            HttpResponse response = client.execute(post);
                                            // Get the response
                                            BufferedReader rd = new BufferedReader
                                                    (new InputStreamReader(response.getEntity().getContent()));
                                            String result = "";
                                            String line = "";
                                            while ((line = rd.readLine()) != null) {
                                                result += line;
                                            }
                                            if(result != null) {
                                                JSONObject resultEvent = new JSONObject(result.toString());
                                                int responseCode = (int)resultEvent.get("response_code");
                                                message = (String) resultEvent.get("message");
                                                if(responseCode == Constants.RESPONSE_CODE_APP_SUCCESS){
                                                    eRchargeDB.deleteUserInfo();
                                                    Intent intentLogin = new Intent(getBaseContext(), Login.class);
                                                    startActivity(intentLogin);
                                                }
                                                else if(responseCode == Constants.ERROR_CODE_APP_INVALID_SESSION)
                                                {
                                                    progressInit.dismiss();
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), "Invalid session. Please try again later.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                else if(responseCode == Constants.ERROR_CODE_APP_LOGOUT_FAILED)
                                                {
                                                    progressInit.dismiss();
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), "Unable to logout. Please try again later.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    message = "Invalid response code:"+responseCode;
                                                    progressInit.dismiss();
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                            else
                                            {
                                                progressInit.dismiss();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getBaseContext(), "Invalid response from the server while verifying login.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                        catch(Exception ex)
                                        {
                                            message = ex.toString();
                                            progressInit.dismiss();
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(getBaseContext(), "Login error:"+message, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                };
                                logoutThread.start();
                            }
                            catch(Exception ex)
                            {
                                Toast.makeText(RechargeMenu.this, "System error:"+ex.toString(), Toast.LENGTH_SHORT).show();
                            }

                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "Unable to logout.Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
    }

    public void managePackageRecharge()
    {
        try
        {
            final ProgressDialog progress = new ProgressDialog(RechargeMenu.this);
            progress.setTitle("Processing");
            progress.setMessage("Retrieving package details...");
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
                        HttpPost post = new HttpPost(baseUrl + Constants.URL_PACKAGE_DETAILS);

                        List<NameValuePair> nameValuePairs = new ArrayList<>();

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
                                    JSONArray operatorList = resultEvent.getJSONArray("operator_list");
                                    JSONArray packageList = resultEvent.getJSONArray("package_list");

                                    eRchargeDB.addOperators(operatorList);
                                    eRchargeDB.addPackages(packageList);

                                    Intent intentTopUpPackage = new Intent(getBaseContext(), PackageRecharge.class);
                                    startActivity(intentTopUpPackage);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PAGE_DBBL) {
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
    }
}
