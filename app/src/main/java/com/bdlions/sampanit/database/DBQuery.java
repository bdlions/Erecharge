package com.bdlions.sampanit.database;

/**
 * Created by sampanit on 22/06/16.
 */
public class DBQuery {
 public static String create_user =  "create table " + QueryField.TABLE_USERS +" (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id TEXT, username TEXT,password TEXT, opcode TEXT)" ;
 //public static String create_user =  "create table " + QueryField.TABLE_USERS +" (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT,password TEXT, opcode TEXT)" ;
}