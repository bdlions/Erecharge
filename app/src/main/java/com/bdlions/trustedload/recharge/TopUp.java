package com.bdlions.trustedload.recharge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bdlions.trustedload.database.DatabaseHelper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TopUp extends AppCompatActivity {
    private static String baseUrl = "";
    private static int userId = 0;
    private static String sessionId = "";
    private static Button buttonTopupSend;
    private static AutoCompleteTextView actvCellNumber;
    private static EditText editTextAmount;
    private static RadioGroup radioGroupTopupPackage;
    private static String message = "";
    private static DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        databaseHelper = DatabaseHelper.getInstance(this);
        radioGroupTopupPackage = (RadioGroup) findViewById(R.id.radioTypeTopUp);

        ArrayList<String> contactList = databaseHelper.getAllContacts();
        ArrayAdapter<String> adapterContacts = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactList);
        actvCellNumber = (AutoCompleteTextView) findViewById(R.id.actvMobileNumberTopUp);
        actvCellNumber.setAdapter(adapterContacts);

        editTextAmount = (EditText) findViewById(R.id.etAmountTopUp);

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
                return;
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
        onClickButtonTopupMenuBackListener();
    }

    public void onClickButtonTopupMenuBackListener()
    {
        buttonTopupSend = (Button) findViewById(R.id.bSendNowTopUp);
        buttonTopupSend.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            int radioButtonID = radioGroupTopupPackage.getCheckedRadioButtonId();
                            View radioButton = radioGroupTopupPackage.findViewById(radioButtonID);
                            //package id 1 is prepaid and 2 is postpaid. index of prepaid is 0 and postpaid is 1
                            final int packageId = radioGroupTopupPackage.indexOfChild(radioButton)+1;

                            //given cell number validation
                            String phoneString = actvCellNumber.getText().toString();
                            if(phoneString == null || phoneString.equals(""))
                            {
                                Toast.makeText(getApplicationContext(), "Please assign number", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            double doubleAmount = 0.0;
                            String strAmount = editTextAmount.getText().toString();
                            try
                            {
                                doubleAmount = Double.parseDouble(strAmount);
                            }
                            catch(Exception ex)
                            {
                                Toast.makeText(getApplicationContext(), "Invalid amount.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(doubleAmount <= 0.0)
                            {
                                Toast.makeText(getApplicationContext(), "Invalid amount.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            final ProgressDialog progress = new ProgressDialog(TopUp.this);
                            progress.setTitle("Processing");
                            progress.setMessage("Wait while executing topup transaction...");
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
                                        HttpPost post = new HttpPost(baseUrl+Constants.URL_TRANSACTION_TOPUP);

                                        List<NameValuePair> nameValuePairs = new ArrayList<>();

                                        nameValuePairs.add(new BasicNameValuePair("number", actvCellNumber.getText().toString()));
                                        nameValuePairs.add(new BasicNameValuePair("amount", editTextAmount.getText().toString()));
                                        nameValuePairs.add(new BasicNameValuePair("user_id", "" + userId));
                                        nameValuePairs.add(new BasicNameValuePair("session_id", "" + sessionId));
                                        nameValuePairs.add(new BasicNameValuePair("operator_type_id", "" + packageId));

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
                                            message = (String)resultEvent.get("message");
                                            if(responseCode == Constants.RESPONSE_CODE_APP_SUCCESS){
                                                String cBalance = resultEvent.get("current_balance").toString();
                                                String cellNo = resultEvent.get("cell_no").toString();
                                                double currentBalance = 0.0;
                                                try
                                                {
                                                    currentBalance =  Double.parseDouble(cBalance);
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
                                                try
                                                {
                                                    if( cellNo != null && !cellNo.equals(""))
                                                    {
                                                        databaseHelper.addContact(cellNo);
                                                    }
                                                    databaseHelper.updateBalance(userId, currentBalance);
                                                }
                                                catch(Exception ex)
                                                {
                                                    message = ex.toString();
                                                    progress.dismiss();
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getBaseContext(), "Error while storing contact number or current balance into the database:"+message, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    return;
                                                }
                                                Intent intentAccount = new Intent(getBaseContext(), RechargeMenu.class);
                                                startActivity(intentAccount);
                                                progress.dismiss();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                            else
                                            {
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                        else
                                        {
                                            progress.dismiss();
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(getBaseContext(), "Invalid response from server.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                    catch (Exception ex) {
                                        message = ex.toString();
                                        progress.dismiss();
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(getBaseContext(), "Server processing error:" + message, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    progress.dismiss();
                                }
                            };
                            bkashThread.start();
                        }
                        catch (Exception ex){
                            Toast.makeText(getApplicationContext(), "System error:"+ex.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

}
