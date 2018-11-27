package me.pjookim.arkq;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServerQueue {
    String BASEURL = "http://arkq.cafe24app.com/";

    @GET("/")
    Call<JsonObject> getQueue(@Query("status") String status);

}