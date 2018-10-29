package com.bdlions.trustedload.recharge;

/**
 * Created by nazmul on 4/5/2016.
 */
public class Constants {

    public static final String URL_BASEURL = "http://signtechbd.com:4040/getbaseurl";
    public static final String URL_LOGIN = "androidappv4/auth/login";
    public static final String URL_PIN = "androidappv4/auth/pin";
    public static final String URL_PACKAGE_DETAILS = "androidappv4/transaction/get_package_details";
    public static final String URL_TRANSACTION_TOPUP = "androidappv4/transaction/topup";
    public static final String URL_TRANSACTION_LIST_TOPUP = "androidappv4/transaction/get_topup_transaction_list";
    public static final String URL_TRANSACTION_BKASH = "androidappv4/transaction/bkash";
    public static final String URL_TRANSACTION_LIST_BKASH = "androidappv4/transaction/get_bkash_transaction_list";
    public static final String URL_TRANSACTION_DBBL = "androidappv4/transaction/dbbl";
    public static final String URL_TRANSACTION_LIST_DBBL = "androidappv4/transaction/get_dbbl_transaction_list";
    public static final String URL_TRANSACTION_MCASH = "androidappv4/transaction/mcash";
    public static final String URL_TRANSACTION_LIST_MCASH = "androidappv4/transaction/get_mcash_transaction_list";
    public static final String URL_TRANSACTION_UCASH = "androidappv4/transaction/ucash";
    public static final String URL_TRANSACTION_LIST_UCASH = "androidappv4/transaction/get_ucash_transaction_list";
    public static final String URL_TRANSACTION_LIST_PAYMENT = "androidappv4/transaction/get_payment_transaction_list";
    public static final String URL_UPDATE_PASSWORD = "androidappv4/auth/update_password";
    public static final String URL_LOGOUT = "androidappv4/auth/logout";

    public static final int PAGE_BKASH = 1;
    public static final int PAGE_BKASH_BACK = 1;
    public static final int PAGE_BKASH_TRANSACTION_SUCCESS = 2;
    public static final int PAGE_BKASH_SERVER_UNAVAILABLE = 3;
    public static final int PAGE_BKASH_SERVER_ERROR= 4;
    public static final int PAGE_BKASH_SESSION_EXPIRED = 5;

    public static final int PAGE_DBBL = 2;
    public static final int PAGE_DBBL_BACK = 1;
    public static final int PAGE_DBBL_TRANSACTION_SUCCESS = 2;
    public static final int PAGE_DBBL_SERVER_UNAVAILABLE = 3;
    public static final int PAGE_DBBL_SERVER_ERROR= 4;
    public static final int PAGE_DBBL_SESSION_EXPIRED = 5;

    public static final int PAGE_MCASH = 3;
    public static final int PAGE_MCASH_BACK = 1;
    public static final int PAGE_MCASH_TRANSACTION_SUCCESS = 2;
    public static final int PAGE_MCASH_SERVER_UNAVAILABLE = 3;
    public static final int PAGE_MCASH_SERVER_ERROR= 4;
    public static final int PAGE_MCASH_SESSION_EXPIRED = 5;

    public static final int PAGE_UCASH = 4;
    public static final int PAGE_UCASH_BACK = 1;
    public static final int PAGE_UCASH_TRANSACTION_SUCCESS = 2;
    public static final int PAGE_UCASH_SERVER_UNAVAILABLE = 3;
    public static final int PAGE_UCASH_SERVER_ERROR= 4;
    public static final int PAGE_UCASH_SESSION_EXPIRED = 5;

    public static final int PAGE_TOPUP = 5;
    public static final int PAGE_TOPUP_TRANSACTION_SUCCESS = 2;
    public static final int PAGE_TOPUP_SERVER_UNAVAILABLE = 3;
    public static final int PAGE_TOPUP_SERVER_ERROR=4;
    public static final int PAGE_TOPUP_SESSION_EXPIRED = 5;


    public static final int SERVICE_TYPE_ID_BKASH_CASHIN = 1;
    public static final int SERVICE_TYPE_ID_BKASH_CASHOUT = 10;
    public static final int SERVICE_TYPE_ID_DBBL_CASHIN = 2;
    public static final int SERVICE_TYPE_ID_DBBL_CASHOUT = 20;
    public static final int SERVICE_TYPE_ID_MCASH_CASHIN = 3;
    public static final int SERVICE_TYPE_ID_MCASH_CASHOUT = 30;
    public static final int SERVICE_TYPE_ID_UCASH_CASHIN = 4;
    public static final int SERVICE_TYPE_ID_UCASH_CASHOUT = 40;
    public static final int SERVICE_TYPE_ID_TOPUP_GP = 101;
    public static final int SERVICE_TYPE_ID_TOPUP_ROBI = 102;
    public static final int SERVICE_TYPE_ID_TOPUP_BANGLALINK = 103;
    public static final int SERVICE_TYPE_ID_TOPUP_AIRTEL = 104;
    public static final int SERVICE_TYPE_ID_TOPUP_TELETALK = 105;

    public static final int SERVICE_TYPE_TOPUP_HISTORY_FLAG = 5;
    public static final String FIRST_COLUMN = "First";
    public static final String SECOND_COLUMN = "Second";
    public static final String THIRD_COLUMN = "Third";
    public static final String FOURTH_COLUMN = "Fourth";
    public static final String FIFTH_COLUMN = "Fifth";

    public static final String SERVICE_TYPE_TITLE_BKASH_CASHIN = "bKash";
    public static final String SERVICE_TYPE_TITLE_DBBL_CASHIN = "DBBL";
    public static final String SERVICE_TYPE_TITLE_UCASH_CASHIN = "UCash";
    public static final String SERVICE_TYPE_TITLE_MCASH_CASHIN = "mCash";
    public static final String SERVICE_TYPE_TITLE_TOPUP = "TopUp";
    public static final String SERVICE_TYPE_TITLE_TOPUP_PACKAGE = "Package";
    public static final String TRANSACTION_HISTROY_TITLE = "History";
    public static final String ACCOUNT_TITLE = "Account";
    public static final String TITLE_LOGOUT = "Logout";
    public static final String SERVICE_TYPE_TITLE_BKASH_HISTORY = "bKash History";
    public static final String SERVICE_TYPE_TITLE_DBBL_HISTORY = "DBBL History";
    public static final String SERVICE_TYPE_TITLE_UCASH_HISTORY = "UCash History";
    public static final String SERVICE_TYPE_TITLE_MCASH_HISTORY = "mCash History";
    public static final String SERVICE_TYPE_TITLE_TOPUP_HISTORY = "TopUp History";


    public static final int RESPONSE_CODE_APP_SUCCESS = 2000;

    public static final int ERROR_CODE_APP_INVALID_LOGIN                = 6001;
    public static final int ERROR_CODE_APP_INVALID_SESSION              = 6002;
    public static final int ERROR_CODE_APP_INVALID_PIN                  = 6003;
    public static final int ERROR_CODE_APP_INVALID_USER                 = 6004;
    public static final int ERROR_CODE_APP_UPDATE_PASSWORD_FAILED       = 6005;
    public static final int ERROR_CODE_APP_LOGOUT_FAILED                = 6015;
    public static final int ERROR_CODE_APP_INVALID_OPERATOR             = 6016;

}
