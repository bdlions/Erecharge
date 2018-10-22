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
import android.widget.Button;
import android.widget.EditText;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity {
    private ConnectivityManager cm;
    private NetworkInfo netInfo;
    private static TextView tvOPCode, tvLoginHeader;
    private static EditText etLoginUserName, etPassword, etOPCode;
    private static String baseUrl = "";
    private static int localUserId = 0;
    private static String sessionId = "";
    private static String pinCode = "";
    private static String opCode = "";
    private static Button buttonLogin;
    private static DatabaseHelper databaseHelper;
    private static String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvLoginHeader = (TextView) findViewById(R.id.tvLoginHeader);
        tvOPCode = (TextView) findViewById(R.id.tvOPCode);
        etOPCode = (EditText) findViewById(R.id.etOPCode);

        etLoginUserName = (EditText) findViewById(R.id.etLoginUserName);
        etPassword = (EditText) findViewById(R.id.etPassword);

        databaseHelper = DatabaseHelper.getInstance(this);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = cm.getActiveNetworkInfo();

        if(netInfo == null || !netInfo.isConnected())
        {
            Toast.makeText(getApplicationContext(), "Please connect to internet first.", Toast.LENGTH_SHORT).show();
        }
        else if(databaseHelper.isUserExists())
        {
            JSONObject localUserInfo =  databaseHelper.getUserInfo();
            try {
                localUserId = (int) localUserInfo.get("userId");
                baseUrl = (String) localUserInfo.get("baseUrl");
                sessionId = (String) localUserInfo.get("sessionId");
                pinCode = (String) localUserInfo.get("pinCode");
                opCode = databaseHelper.getOPCode();
                getUserInfo();
            } catch (JSONException ex) {
                //if there is an exception then delete user info from the local database
                databaseHelper.deleteUserInfo();
            }
        }
        opCode = databaseHelper.getOPCode();
        if(opCode == null || opCode.isEmpty())
        {
            //show opcode field
            tvOPCode.setVisibility(View.VISIBLE);
            etOPCode.setVisibility(View.VISIBLE);
            tvLoginHeader.setText("Recharge V3");
        }
        else
        {
            if(opCode.length() > 1)
            {
                tvLoginHeader.setText(opCode.substring(0,1).toUpperCase()+opCode.substring(1)+ " V3");
            }
            else
            {
                tvLoginHeader.setText(opCode.toUpperCase()+ " V3");
            }
        }
        onClickButtonLoginListener();
    }

    public void getUserInfo(){
        try
        {
            final ProgressDialog progressInit = new ProgressDialog(Login.this);
            progressInit.setTitle("Login");
            progressInit.setMessage("Authenticating user...");
            progressInit.show();

            final Thread initThread = new Thread() {
                @Override
                public void run()
                {
                    try
                    {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(Constants.URL_BASEURL);

                        List<NameValuePair> nameValuePairs = new ArrayList<>();
                        nameValuePairs.add(new BasicNameValuePair("code", opCode));

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
                            int responseCode = (int)resultEvent.get("responseCode");
                            if(responseCode == Constants.RESPONSE_CODE_APP_SUCCESS)
                            {
                                baseUrl = (String) resultEvent.get("result");
                                if( baseUrl == null || baseUrl.equals(""))
                                {
                                    progressInit.dismiss();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getBaseContext(), "Invalid server url:"+baseUrl, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else
                                {
                                    databaseHelper.updateBaseURL(localUserId, baseUrl);
                                    try
                                    {
                                        Thread autoLoginThread = new Thread() {
                                            @Override
                                            public void run()
                                            {
                                                try
                                                {
                                                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                                    StrictMode.setThreadPolicy(policy);
                                                    HttpClient client = new DefaultHttpClient();
                                                    HttpPost post = new HttpPost(baseUrl + Constants.URL_PIN);
                                                    List<NameValuePair> nameValuePairs = new ArrayList<>();
                                                    nameValuePairs.add(new BasicNameValuePair("user_id", localUserId+""));
                                                    nameValuePairs.add(new BasicNameValuePair("pin", pinCode));
                                                    nameValuePairs.add(new BasicNameValuePair("total_contacts", "" + databaseHelper.getTotalContacts()));
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
                                                    if(result != null)
                                                    {
                                                        JSONObject resultEvent = new JSONObject(result.toString());
                                                        int responseCode = 0;
                                                        try
                                                        {
                                                            responseCode = (int)resultEvent.get("response_code");
                                                        }
                                                        catch(Exception ex)
                                                        {
                                                            message = "Invalid response code from server:" + ex.toString();
                                                            progressInit.dismiss();
                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                        if(responseCode == Constants.RESPONSE_CODE_APP_SUCCESS)
                                                        {
                                                            try
                                                            {
                                                                JSONObject jsonResultEvent = (JSONObject) resultEvent.get("result_event");
                                                                sessionId = jsonResultEvent.get("session_id").toString();
                                                                String strUserInfo = jsonResultEvent.get("user_info").toString();
                                                                JSONObject jsonUserInfo  = new JSONObject(strUserInfo);
                                                                String userName =    jsonUserInfo.get("user_name").toString();
                                                                databaseHelper.updateUserName(localUserId, userName);
                                                                Intent intent = new Intent(getBaseContext(), RechargeMenu.class);
                                                                //getting service id list
                                                                JSONArray serviceIdList = jsonResultEvent.getJSONArray("service_id_list");
                                                                int[] serviceList = new int[serviceIdList.length()];
                                                                for (int i = 0; i < serviceIdList.length(); i++)
                                                                {
                                                                    int serviceId = (int)serviceIdList.get(i);
                                                                    serviceList[i] = serviceId;
                                                                }
                                                                databaseHelper.deleteAllServices();
                                                                databaseHelper.addServices(localUserId, serviceList);
                                                                JSONArray contactList = jsonResultEvent.getJSONArray("contact_list");
                                                                for (int i = 0; i < contactList.length(); i++)
                                                                {
                                                                    String contactNumber = (String)contactList.get(i);
                                                                    databaseHelper.addContact(contactNumber);
                                                                }
                                                                double currentBalance = 0;
                                                                try
                                                                {
                                                                    currentBalance = Double.parseDouble(jsonResultEvent.get("current_balance").toString());
                                                                }
                                                                catch(Exception ex)
                                                                {
                                                                    message = ex.toString();
                                                                    progressInit.dismiss();
                                                                    runOnUiThread(new Runnable() {
                                                                        public void run() {
                                                                            Toast.makeText(getBaseContext(), "Invalid current balance from the server.Error:"+message, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                    return;
                                                                }
                                                                databaseHelper.updateBalance(localUserId, currentBalance);
                                                                startActivity(intent);
                                                                progressInit.dismiss();
                                                            }
                                                            catch(Exception ex)
                                                            {
                                                                message = ex.toString();
                                                                progressInit.dismiss();
                                                                runOnUiThread(new Runnable() {
                                                                    public void run() {
                                                                        Toast.makeText(getBaseContext(), "Invalid datafrom the server.Error:"+message, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                                return;
                                                            }

                                                        }
                                                        else if(responseCode == Constants.ERROR_CODE_APP_INVALID_PIN)
                                                        {
                                                            progressInit.dismiss();
                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    Toast.makeText(getBaseContext(), "Invalid user pin. Please login again.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                            return;
                                                        }
                                                        else if(responseCode == Constants.ERROR_CODE_APP_INVALID_USER)
                                                        {
                                                            progressInit.dismiss();
                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    Toast.makeText(getBaseContext(), "Your session is expired. Please login again.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                            return;
                                                        }
                                                    }
                                                    else
                                                    {
                                                        message = "Invalid response from the server while processing auto login.";
                                                        progressInit.dismiss();
                                                        runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        return;
                                                    }
                                                }
                                                catch (Exception ex) {
                                                    message = "Server communication error:" + ex.toString();
                                                    progressInit.dismiss();
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    return;
                                                }
                                                progressInit.dismiss();
                                            }
                                        };
                                        autoLoginThread.start();
                                    }
                                    catch (Exception ex){
                                        message = "System error:" + ex.toString();
                                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            else
                            {
                                message = "Invalid response code to get server url:"+responseCode;
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
                                    Toast.makeText(getBaseContext(), "Invalid response to get server url.", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getBaseContext(), "Error while authenticating user::"+message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            };
            initThread.start();
        }
        catch (Exception ex){
            Toast.makeText(Login.this, "System error1:"+ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickButtonLoginListener(){
        buttonLogin = (Button)findViewById(R.id.bLogin);
        buttonLogin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            netInfo = cm.getActiveNetworkInfo();
                            if(netInfo == null || !netInfo.isConnected())
                            {
                                Toast.makeText(getApplicationContext(), "Please connect to internet first.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            final String username = etLoginUserName.getText().toString();
                            final String password = etPassword.getText().toString();
                            if(opCode == null || opCode.isEmpty())
                            {
                                opCode = etOPCode.getText().toString();
                                databaseHelper.addOpcode(opCode);
                            }

                            if(username == null || username.equals(""))
                            {
                                Toast.makeText(getApplicationContext(), "Please assign user name.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(password == null || password.equals(""))
                            {
                                Toast.makeText(getApplicationContext(), "Please assign password.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            /*if(opcode == null || opcode.equals(""))
                            {
                                Toast.makeText(getApplicationContext(), "Please assign opcode.", Toast.LENGTH_SHORT).show();
                                return;
                            }*/

                            final ProgressDialog progressInit = new ProgressDialog(Login.this);
                            progressInit.setTitle("Login");
                            progressInit.setMessage("Authenticating user...");
                            progressInit.show();

                            final Thread initThread = new Thread() {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                        StrictMode.setThreadPolicy(policy);
                                        HttpClient client = new DefaultHttpClient();
                                        HttpPost post = new HttpPost(Constants.URL_BASEURL);

                                        List<NameValuePair> nameValuePairs = new ArrayList<>();
                                        nameValuePairs.add(new BasicNameValuePair("code", opCode));

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
                                            int responseCode = (int)resultEvent.get("responseCode");
                                            if(responseCode == Constants.RESPONSE_CODE_APP_SUCCESS){
                                                baseUrl = (String) resultEvent.get("result");
                                                if( baseUrl == null || baseUrl.equals(""))
                                                {
                                                    progressInit.dismiss();
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), "Invalid server url:"+baseUrl, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    try
                                                    {
                                                        final Thread loginThread = new Thread() {
                                                            @Override
                                                            public void run()
                                                            {
                                                                try
                                                                {
                                                                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                                                    StrictMode.setThreadPolicy(policy);
                                                                    HttpClient client = new DefaultHttpClient();
                                                                    HttpPost post = new HttpPost(baseUrl+Constants.URL_LOGIN);

                                                                    List<NameValuePair> nameValuePairs = new ArrayList<>();
                                                                    nameValuePairs.add(new BasicNameValuePair("user_name", etLoginUserName.getText().toString()));
                                                                    nameValuePairs.add(new BasicNameValuePair("password", etPassword.getText().toString()));

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
                                                                            JSONObject jsonResultEvent = (JSONObject) resultEvent.get("result_event");
                                                                            String strUserInfo = jsonResultEvent.get("user_info").toString();
                                                                            JSONObject jsonUserInfo  = new JSONObject(strUserInfo);
                                                                            int userId =    Integer.parseInt(jsonUserInfo.get("user_id").toString());
                                                                            String sessionId = jsonResultEvent.get("session_id").toString();
                                                                            String companyName = jsonResultEvent.get("company_name").toString();
                                                                            databaseHelper.deleteUserInfo();
                                                                            if(databaseHelper.createUser(userId, etLoginUserName.getText().toString(), etPassword.getText().toString(), "", baseUrl, sessionId, 0, companyName))
                                                                            {
                                                                                //intent to verify pin code
                                                                                Intent intent = new Intent(getBaseContext(), PinCode.class);
                                                                                intent.putExtra("BASE_URL", baseUrl);
                                                                                intent.putExtra("USER_ID", userId);
                                                                                intent.putExtra("SESSION_ID", sessionId);
                                                                                startActivity(intent);
                                                                                progressInit.dismiss();
                                                                            }
                                                                            else
                                                                            {
                                                                                message = "System error while saving user info. Please try again.";
                                                                                progressInit.dismiss();
                                                                                runOnUiThread(new Runnable() {
                                                                                    public void run() {
                                                                                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                        else if(responseCode == Constants.ERROR_CODE_APP_INVALID_SESSION)
                                                                        {
                                                                            progressInit.dismiss();
                                                                            runOnUiThread(new Runnable() {
                                                                                public void run() {
                                                                                    Toast.makeText(getBaseContext(), "Unable to create your session. Please try again later.", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }
                                                                        else if(responseCode == Constants.ERROR_CODE_APP_INVALID_LOGIN)
                                                                        {
                                                                            progressInit.dismiss();
                                                                            runOnUiThread(new Runnable() {
                                                                                public void run() {
                                                                                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
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
                                                        loginThread.start();
                                                    }
                                                    catch (Exception ex){
                                                        Toast.makeText(Login.this, "System error2:"+ex.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                message = "Invalid response code to get server url:"+responseCode;
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
                                                    Toast.makeText(getBaseContext(), "Invalid response to get server url.", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(getBaseContext(), "Server url error:"+message, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            };
                            initThread.start();
                        }
                        catch (Exception ex){
                            Toast.makeText(Login.this, "System error1:"+ex.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
}
