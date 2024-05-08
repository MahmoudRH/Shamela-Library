package com.folioreader.network

import org.readium.r2.shared.Locator
import retrofit2.http.GET
import retrofit2.http.Query

interface R2StreamerApi {

    @GET("search") @Gson
   suspend fun search(@Query("spineIndex") spineIndex: Int, @Query("query") query: String): List<Locator>
}