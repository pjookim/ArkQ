package me.pjookim.arkq;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServerTimer {
    String BASEURL = "http://arkq.cafe24app.com/";

    @GET("/")
    Call<JsonObject> getTimer(@Query("status") String status, @Query("time") String time, @Query("server") String serverId);

}