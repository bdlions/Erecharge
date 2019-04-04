package com.bdlions.trustedload.utils;

import com.bdlions.trustedload.recharge.Constants;

public class ServiceUtils {
    public static int getTopupServiceId(String cellNumber)
    {
        int serviceId = 0;
        if(cellNumber == null || cellNumber.isEmpty())
        {
            return 0;
        }
        if(cellNumber.startsWith("+"))
        {
            cellNumber = cellNumber.substring(1, cellNumber.length());
        }
        if(cellNumber.startsWith("88"))
        {
            cellNumber = cellNumber.substring(2, cellNumber.length());
        }
        if(cellNumber.startsWith("017") || cellNumber.startsWith("013"))
        {
            serviceId = Constants.SERVICE_TYPE_ID_TOPUP_GP;
        }
        else if(cellNumber.startsWith("018"))
        {
            serviceId = Constants.SERVICE_TYPE_ID_TOPUP_ROBI;
        }
        else if(cellNumber.startsWith("014") || cellNumber.startsWith("019"))
        {
            serviceId = Constants.SERVICE_TYPE_ID_TOPUP_BANGLALINK;
        }
        else if(cellNumber.startsWith("016"))
        {
            serviceId = Constants.SERVICE_TYPE_ID_TOPUP_AIRTEL;
        }
        else if(cellNumber.startsWith("015"))
        {
            serviceId = Constants.SERVICE_TYPE_ID_TOPUP_TELETALK;
        }
        return serviceId;
    }
}
