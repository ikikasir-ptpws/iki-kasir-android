package com.ptpws.ikikasir.feature.referral.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReferralRetrofitApi {

    @GET("referrals/overview")
    suspend fun getReferralOverview(
        @Query("email") email: String
    ): Response<ReferralApiResponse<ReferralOverviewResponseDto>>

    @POST("referrals/submit-client")
    suspend fun submitClient(
        @Body request: SubmitClientRequestDto
    ): Response<ReferralApiResponse<SubmitClientResponseDto>>

    @POST("referrals/withdraw")
    suspend fun requestWithdrawal(
        @Body request: ReferralWithdrawRequestDto
    ): Response<ReferralApiResponse<Any>>
}
