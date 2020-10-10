package com.example.employee;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CategoryInterface {
    @GET("v1/tags")
    abstract Call<String> STRING_CALL(

    );
}
