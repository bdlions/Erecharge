package com.bdlions.trustedload.recharge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.AdapterView;
import android.widget.Toast;

import com.bdlions.trustedload.bean.OperatorInfo;
import com.bdlions.trustedload.bean.PackageInfo;
import com.bdlions.trustedload.database.DatabaseHelper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

public class PackageRecharge extends AppCompatActivity {
    private static String baseUrl = "";
    private static int userId = 0;
    private static String sessionId = "";
    private static String message = "";

    private static DatabaseHelper databaseHelper;
    private static AutoCompleteTextView actvCellNumber;
    private static EditText etAmount;
    Spinner operator_spnr, package_spnr;
    private static Button buttonPackageSend;
    private static RadioGroup radioGroupPackage;

    ArrayList<PackageInfo> packageList = new ArrayList<>();
    ArrayAdapter<PackageInfo> packageAdapter;

    OperatorInfo selectedOperatorInfo;
    PackageInfo selectedPackageInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_recharge);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
            Toast.makeText(getApplicationContext(), "Invalid user.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> contactList = databaseHelper.getAllContacts();
        ArrayAdapter<String> adapterContacts = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactList);
        actvCellNumber = (AutoCompleteTextView) findViewById(R.id.actvMobileNumberPackage);
        actvCellNumber.setAdapter(adapterContacts);

        //disabling amount field to be editable
        etAmount = (EditText) findViewById(R.id.etAmountPackage);
        etAmount.setKeyListener(null);

        radioGroupPackage = (RadioGroup) findViewById(R.id.radioTypePackage);

        operator_spnr = (Spinner) findViewById(R.id.spMobileOperator);
        package_spnr = (Spinner) findViewById(R.id.spMobileOperatorPackage);

        ArrayAdapter<OperatorInfo> operatorAdapter = new ArrayAdapter<OperatorInfo>( this, android.R.layout.simple_spinner_item, databaseHelper.getAllOperators());
        operator_spnr.setAdapter(operatorAdapter);

        packageList.add(new PackageInfo(0, 0, "Select", 0));
        packageAdapter = new ArrayAdapter<PackageInfo>( this, android.R.layout.simple_spinner_item, packageList);
        package_spnr.setAdapter(packageAdapter);

        operator_spnr.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        selectedOperatorInfo = (OperatorInfo) operator_spnr.getSelectedItem();
                        packageAdapter.clear();
                        packageList.clear();
                        packageList = databaseHelper.getAllPackages(selectedOperatorInfo.getId());
                        for (int counter = 0; counter < packageList.size(); counter++) {
                            packageAdapter.add(packageList.get(counter));
                        }
                        packageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub
                    }
                }
        );
        package_spnr.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {

                        selectedPackageInfo = (PackageInfo)package_spnr.getSelectedItem();
                        etAmount.setText(selectedPackageInfo.getAmount() + "");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub

                    }

                }
        );
        onClickButtonPackageSendListener();
    }

    public void onClickButtonPackageSendListener()
    {
        buttonPackageSend = (Button) findViewById(R.id.bSendNowPackageRecharge);
        buttonPackageSend.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            if(selectedOperatorInfo == null || selectedOperatorInfo.getId() <= 0)
                            {
                                Toast.makeText(getApplicationContext(), "Please select an operator", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(selectedPackageInfo == null || selectedPackageInfo.getId() <= 0)
                            {
                                Toast.makeText(getApplicationContext(), "Please select a package", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            int radioButtonID = radioGroupPackage.getCheckedRadioButtonId();
                            View radioButton = radioGroupPackage.findViewById(radioButtonID);
                            //package id 1 is prepaid and 2 is postpaid. index of prepaid is 0 and postpaid is 1
                            final int packageId = radioGroupPackage.indexOfChild(radioButton)+1;

                            //given cell number validation
                            String phoneString = actvCellNumber.getText().toString();
                            if(phoneString == null || phoneString.equals(""))
                            {
                                Toast.makeText(getApplicationContext(), "Please assign number", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            double doubleAmount = 0.0;
                            String strAmount = etAmount.getText().toString();
                            try
                            {
                                doubleAmount = Double.parseDouble(strAmount);
                            }
                            catch(Exception ex)
                            {
                                Toast.makeText(getApplicationContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(doubleAmount <= 0.0)
                            {
                                Toast.makeText(getApplicationContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            final ProgressDialog progress = new ProgressDialog(PackageRecharge.this);
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
                                        nameValuePairs.add(new BasicNameValuePair("amount", etAmount.getText().toString()));
                                        nameValuePairs.add(new BasicNameValuePair("user_id", "" + userId));
                                        nameValuePairs.add(new BasicNameValuePair("session_id", "" + sessionId));
                                        nameValuePairs.add(new BasicNameValuePair("operator_type_id", "" + packageId));
                                        nameValuePairs.add(new BasicNameValuePair("service_id", "" + selectedOperatorInfo.getId()));

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
                                                try
                                                {
                                                    databaseHelper.updateBalance(userId, Double.parseDouble(cBalance));
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
