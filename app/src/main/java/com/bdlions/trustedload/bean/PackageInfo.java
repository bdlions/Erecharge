package com.bdlions.trustedload.bean;

/**
 * Created by nazmul on 2/17/2017.
 */
public class PackageInfo {
    private int id;
    private int operatorId;
    private String title;
    private double amount;

    public PackageInfo(int id, int operatorId, String title, double amount) {
        this.id = id;
        this.operatorId = operatorId;
        this.title = title;
        this.amount = amount;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }


    //to display object as a string in spinner
    @Override
    public String toString() {
        return title;
    }
}
