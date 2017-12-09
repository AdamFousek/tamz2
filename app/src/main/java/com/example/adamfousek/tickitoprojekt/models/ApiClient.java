package com.example.adamfousek.tickitoprojekt.models;

import com.example.adamfousek.tickitoprojekt.models.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Created by adamfousek on 28.11.17.
 */

public interface ApiClient {

    // Retrofit - ziskání všech událostí
    @GET("api/v1/event/")
    Call<User> getUser(@Header("Authorization") String authHeader);

    @GET("api/v1/event/{id}/code/")
    Call<Codes> getCodes(@Header("Authorization") String authHeader, @Path("id") String id);

    @GET("api/v1/code/{code}/validate/")
    Call<Code> checkCode(@Header("Authorization") String authHeader, @Path("code") String code);

}
