package com.pure.gothic.hackathon.idhackandroid.network;

/**
 * Created by quki on 2016-02-13.
 */
public class NetworkConfig {

    // Server user account url
    public static String URL_ACCOUNT = "http://www.qukihub.com:8080/idhack2016-server/logregi/index.php";

    // Server message data using MySQL
    // !important now we are using Firebase, not MySQL and PHP
    public static String URL_INSERT = "http://52.8.72.60/idhack/inserting.php";
    public static String URL_SELECT = "http://52.8.72.60/idhack/selecting.php";

    public static boolean IS_NETWORK_ON = true;
}
