package com.example.employee;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PostInterface {
    @GET("v1/search")
    abstract Call<String> STRING_CALL(
            @Query("paged") int page,
            @Query("per_page") int perPage
    );
}
