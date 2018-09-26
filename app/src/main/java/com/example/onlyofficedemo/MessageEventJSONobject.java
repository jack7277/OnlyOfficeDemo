package com.example.onlyofficedemo;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MessageEventJSONobject {
       public int statusCode;
       public List<? extends Header> header;
       public JSONObject response;

        public MessageEventJSONobject(int statusCode, List<? extends Header> headers, JSONObject response) {
            this.statusCode = statusCode;
            this.header = headers;
            this.response = response;
        }


}
