package com.bdlions.trustedload.recharge;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bdlions.trustedload.database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.bdlions.trustedload.recharge.Constants.FIFTH_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.FIRST_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.FOURTH_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.SECOND_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.THIRD_COLUMN;

public class Payment extends AppCompatActivity {
    private static DatabaseHelper databaseHelper;
    private ArrayList<HashMap<String, String>> paymentHistoryList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        databaseHelper = DatabaseHelper.getInstance(this);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        try
        {
            String paymentTransactionList = getIntent().getExtras().getString("PAYMENT_TRANSACTION_LIST");
            ListView listView = (ListView) findViewById(R.id.list_view_payment_history);
            paymentHistoryList = new ArrayList<HashMap<String, String>>();
            populateList(paymentTransactionList);
            PaymentHistoryListViewAdapter adapter = new PaymentHistoryListViewAdapter(this, paymentHistoryList);
            listView.setAdapter(adapter);
        }
        catch(Exception ex)
        {
            Toast.makeText(getApplicationContext(), "Please login again. Error:" + ex.toString(), Toast.LENGTH_SHORT).show();
            databaseHelper.deleteUserInfo();
            Intent intentLogin = new Intent(getBaseContext(), Login.class);
            startActivity(intentLogin);
            return;
        }
    }
    private void populateList(String paymentTList) {
        //table header
        HashMap<String, String> temp = new HashMap<String, String>();
        temp.put(FIRST_COLUMN, "User");
        temp.put(SECOND_COLUMN, "Date");
        temp.put(THIRD_COLUMN, "Amount");
        temp.put(FOURTH_COLUMN, "Type");
        temp.put(FIFTH_COLUMN, "Desc");
        paymentHistoryList.add(temp);
        try
        {
            JSONArray paymentTransactionArray = new JSONArray(paymentTList);
            for (int i = 0; i < paymentTransactionArray.length(); i++) {
                JSONObject transactionObject = paymentTransactionArray.getJSONObject(i);
                HashMap<String, String> temp2 = new HashMap<String, String>();
                temp2.put(FIRST_COLUMN, transactionObject.get("username").toString());
                temp2.put(SECOND_COLUMN, transactionObject.get("date").toString());
                temp2.put(THIRD_COLUMN, transactionObject.get("amount").toString());
                temp2.put(FOURTH_COLUMN, transactionObject.get("type").toString());
                temp2.put(FIFTH_COLUMN, transactionObject.get("description").toString());
                paymentHistoryList.add(temp2);
            }
        }
        catch(Exception ex)
        {

        }
    }

}
