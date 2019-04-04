package com.bdlions.trustedload.recharge;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import com.bdlions.trustedload.database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.bdlions.trustedload.recharge.Constants.FIRST_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.FOURTH_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.SECOND_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.THIRD_COLUMN;
public class MCashHistory extends AppCompatActivity {
    private static DatabaseHelper databaseHelper;
    private ArrayList<HashMap<String, String>> mCashHistoryList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m_cash_history);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        databaseHelper = DatabaseHelper.getInstance(this);
        try
        {
            String transactionList = getIntent().getExtras().getString("TRANSACTION_LIST");
            ListView listView = (ListView) findViewById(R.id.list_view_mcash_history);
            mCashHistoryList = new ArrayList<HashMap<String, String>>();
            populateList(transactionList);
            MCashHistoryListViewAdapter adapter = new MCashHistoryListViewAdapter(this, mCashHistoryList);
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

    private void populateList(String transactionList)
    {
        try
        {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp.put(FIRST_COLUMN, "Cell Number");
            temp.put(SECOND_COLUMN, "Amount");
            temp.put(THIRD_COLUMN, "Title");
            temp.put(FOURTH_COLUMN, "Status");
            mCashHistoryList.add(temp);
            JSONArray transactionArray = new JSONArray(transactionList);
            for (int i = 0; i < transactionArray.length(); i++) {
                JSONObject transactionObject = transactionArray.getJSONObject(i);
                HashMap<String, String> temp2 = new HashMap<String, String>();
                temp2.put(FIRST_COLUMN, (String)transactionObject.get("cell_no"));
                temp2.put(SECOND_COLUMN, transactionObject.getDouble("amount") + "");
                temp2.put(THIRD_COLUMN, (String)transactionObject.get("title"));
                temp2.put(FOURTH_COLUMN, (String)transactionObject.get("status"));
                mCashHistoryList.add(temp2);
            }
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
}
