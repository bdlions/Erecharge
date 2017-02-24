package com.bdlions.trustedload.database;

public class DBQuery {
     public static String CREATE_TABLE_OPCODE =  "CREATE TABLE IF NOT EXISTS " + QueryField.TABLE_OPCODE +" (id INTEGER PRIMARY KEY AUTOINCREMENT, opcode TEXT)" ;
     public static String CREATE_TABLE_USERS =  "CREATE TABLE IF NOT EXISTS " + QueryField.TABLE_USERS +" (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER, session_id TEXT, username TEXT, base_url TEXT, password TEXT, pin_code TEXT, balance DOUBLE, company_name TEXT)" ;
     public static String CREATE_TABLE_SERVICES =  "CREATE TABLE IF NOT EXISTS " + QueryField.TABLE_SERVICES +" (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER, service_id INTEGER)" ;
     public static String CREATE_TABLE_OPERATORS =  "CREATE TABLE IF NOT EXISTS " + QueryField.TABLE_OPERATORS+" (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT)" ;
     public static String CREATE_TABLE_PACKAGES =  "CREATE TABLE IF NOT EXISTS " + QueryField.TABLE_PACKAGES +" (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, operator_id INTEGER, amount DOUBLE)" ;
     public static String CREATE_TABLE_CONTACTS =  "CREATE TABLE IF NOT EXISTS " + QueryField.TABLE_CONTACTS+" (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT)" ;
}
