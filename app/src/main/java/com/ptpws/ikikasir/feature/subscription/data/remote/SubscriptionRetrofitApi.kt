package com.ptpws.ikikasir.feature.subscription.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SubscriptionRetrofitApi {
    @GET("subscriptions/check")
    suspend fun checkSubscription(
        @Query("email") email: String
    ): Response<SubscriptionApiResponse>
}
