package com.tech.young.data.api

import android.util.Log
import com.tech.young.BuildConfig
import com.tech.young.data.IndexQuote
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object StockQuoteService {

    private const val BASE_URL = BuildConfig.FINNHUB_BASE_URL
    private const val API_KEY = BuildConfig.FINNHUB_API_KEY

    interface StockApiService {
        @GET("quote")
        suspend fun getQuote(
            @Query("symbol") symbol: String,
            @Query("token") token: String
        ): IndexQuote
    }

    private val apiService: StockApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StockApiService::class.java)
    }

    suspend fun fetchQuotes(symbols: List<String>, apiKey : String): Map<String, IndexQuote> {
        val result = mutableMapOf<String, IndexQuote>()
        supervisorScope {
            symbols.map { symbol ->
                async {
                    try {
                        val quote = withTimeoutOrNull(8000) {
                            apiService.getQuote(symbol, apiKey)
                        }
                        if (quote != null) {
                            result[symbol] = quote
                            Log.i("QuoteFetcher", "$symbol: $quote")
                        } else {
                            Log.w("QuoteFetcher", "$symbol: fetch timed out")
                        }
                    } catch (e: Exception) {
                        Log.e("QuoteFetcher", "Error fetching $symbol: ${e.message}", e)
                    }
                }
            }.awaitAll()
        }
        return result
    }

}
