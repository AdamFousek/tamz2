package com.example.adamfousek.tickitoprojekt;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

/**
 * Created by adamfousek on 28.11.17.
 */

public interface UserClient {

    // Retrofit - ziskání všech událostí
    @GET("api/v1/event/")
    Call<User> getUser(@Header("Authorization") String authHeader);

    // @TODO ověření lístků

}
