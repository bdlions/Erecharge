package com.bdlions.trustedload.bean;

/**
 * Created by nazmul on 2/17/2017.
 */
public class OperatorInfo {
    private int id;
    private String title;

    public OperatorInfo(int id, String title) {
        this.id = id;
        this.title = title;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    //to display object as a string in spinner
    @Override
    public String toString() {
        return title;
    }

    /*@Override
    public boolean equals(Object obj) {
        if(obj instanceof OperatorInfo){
            OperatorInfo c = (OperatorInfo )obj;
            if(c.getName().equals(name) && c.getId()==id ) return true;
        }

        return false;
    }*/

}
