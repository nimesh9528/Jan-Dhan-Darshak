package jan.dhan.darshak.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("nearbysearch/json?")
    fun getPlaces(
        @Query("location") location: String?,
        @Query("keyword") keyword: String?,
        @Query("language") language: String?,
        @Query("opennow") openNow: String?,
        @Query("radius") radius: Int?,
        @Query("rankby") rankBy: String?,
        @Query("type") type: String?,
        @Query("key") api: String?
    ): Call<String>
}