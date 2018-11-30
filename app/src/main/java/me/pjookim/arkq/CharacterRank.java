package me.pjookim.arkq;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CharacterRank {
    String BASEURL = "http://arkq.cafe24app.com/";

    @GET("rank/")
    Call<JsonObject> getRanking(@Query("nick") String nickname, @Query("item") String item);

}