package com.bdlions.trustedload.recharge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class PinCode extends AppCompatActivity {
    private static EditText editPinCode;
    private static Button buttonPinCode;
    private static String baseUrl = "";
    private static String sessionId = "";
    private static int userId = 0;
    private static DatabaseHelper databaseHelper;
    private static String message = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_code);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editPinCode = (EditText) findViewById(R.id.etPinCode);

        Bundle extras = getIntent().getExtras();
        databaseHelper = DatabaseHelper.getInstance(this);
        if (extras != null)
        {
            userId = (int) getIntent().getExtras().get("USER_ID");
            baseUrl = getIntent().getExtras().getString("BASE_URL");
            sessionId = getIntent().getExtras().getString("SESSION_ID");
        }
        else
        {
            JSONObject localUserInfo =  databaseHelper.getUserInfo();
            try
            {
                userId = (int) localUserInfo.get("userId");
                baseUrl = (String) localUserInfo.get("baseUrl");
                sessionId = (String) localUserInfo.get("sessionId");
            }
            catch (JSONException ex)
            {
                databaseHelper.deleteUserInfo();
                Intent intentLogin = new Intent(getBaseContext(), Login.class);
                startActivity(intentLogin);
            }
        }
        onClickButtonPinCode();
    }

    public void onClickButtonPinCode() {
        buttonPinCode = (Button) findViewById(R.id.bPinCodeVerification);
        buttonPinCode.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            final String pincode = editPinCode.getText().toString();
                            if(pincode == null || pincode.equals(""))
                            {
                                Toast.makeText(getApplicationContext(), "Please assign pin code.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            final ProgressDialog progress = new ProgressDialog(PinCode.this);
                            progress.setTitle("Processing");
                            progress.setMessage("Varifying  user...");
                            progress.show();
                            Thread varificationThread = new Thread() {
                                @Override
                                public void run() {
                                    try
                                    {
                                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                        StrictMode.setThreadPolicy(policy);
                                        HttpClient client = new DefaultHttpClient();
                                        HttpPost post = new HttpPost(baseUrl + Constants.URL_PIN);

                                        List<NameValuePair> nameValuePairs = new ArrayList<>();

                                        nameValuePairs.add(new BasicNameValuePair("user_id", "" + userId));
                                        nameValuePairs.add(new BasicNameValuePair("session_id", "" + sessionId));
                                        nameValuePairs.add(new BasicNameValuePair("total_contacts", "" + databaseHelper.getTotalContacts()));
                                        nameValuePairs.add(new BasicNameValuePair("pin", editPinCode.getText().toString()));

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
                                                message = ex.toString();
                                                progress.dismiss();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getBaseContext(), "Invalid response code from server:"+message, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                return;
                                            }
                                            if(responseCode == Constants.RESPONSE_CODE_APP_SUCCESS)
                                            {
                                                try
                                                {
                                                    JSONObject jsonResultEvent = (JSONObject) resultEvent.get("result_event");
                                                    databaseHelper.updatePinCode(userId, pincode);
                                                    //getting service id list
                                                    JSONArray serviceIdList = jsonResultEvent.getJSONArray("service_id_list");
                                                    int[] serviceList = new int[serviceIdList.length()];
                                                    for (int i = 0; i < serviceIdList.length(); i++)
                                                    {
                                                        int serviceId = (int)serviceIdList.get(i);
                                                        serviceList[i] = serviceId;
                                                    }
                                                    databaseHelper.deleteAllServices();
                                                    databaseHelper.addServices(userId, serviceList);
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
                                                        progress.dismiss();
                                                        runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                Toast.makeText(getBaseContext(), "Invalid current balance from server:"+message, Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        return;
                                                    }
                                                    databaseHelper.updateBalance(userId, currentBalance);
                                                    Intent intent = new Intent(getBaseContext(), RechargeMenu.class);
                                                    startActivity(intent);
                                                    progress.dismiss();
                                                }
                                                catch(Exception ex)
                                                {
                                                    message = "Exception while processing server response:" + ex.toString();
                                                    progress.dismiss();
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                            else if(responseCode == Constants.ERROR_CODE_APP_INVALID_PIN)
                                            {
                                                progress.dismiss();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getBaseContext(), "Sorry!Invalid user pin!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                            else if(responseCode == Constants.ERROR_CODE_APP_INVALID_USER)
                                            {
                                                progress.dismiss();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getBaseContext(), "Your session is expired.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                        else
                                        {
                                            progress.dismiss();
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(getBaseContext(), "Invalid response from the server.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                    catch(Exception ex)
                                    {
                                        message = "Error while verifying pin:" + ex.toString();
                                        progress.dismiss();
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            };
                            varificationThread.start();
                        }
                        catch (Exception ex)
                        {
                            Toast.makeText(getApplicationContext(), "System Error:"+ex.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
}
