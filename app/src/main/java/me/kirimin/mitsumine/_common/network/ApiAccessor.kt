package me.kirimin.mitsumine._common.network

import android.content.Context
import me.kirimin.mitsumine.BuildConfig

import org.json.JSONObject
import org.scribe.builder.ServiceBuilder
import org.scribe.model.OAuthRequest
import org.scribe.model.Response
import org.scribe.model.Token

import me.kirimin.mitsumine._common.domain.model.Account
import me.kirimin.mitsumine._common.domain.exceptions.ApiRequestException
import me.kirimin.mitsumine._common.network.oauth.Consumer
import me.kirimin.mitsumine._common.network.oauth.HatenaOAuthProvider
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import rx.Observable
import java.io.File
import java.util.concurrent.TimeUnit

object ApiAccessor {

    private val MAX_AGE_SECOND = 60 * 5;
    private val MAX_CACHE_SIZE = 1024 * 1024 * 8.toLong()

    private var defaultClient: OkHttpClient? = null

    fun request(context: Context, url: String): Observable<JSONObject> {
        return stringRequest(context, url).map { response ->
            JSONObject(response)
        }
    }

    fun stringRequest(context: Context, url: String): Observable<String> {
        return Observable.create<String> { subscriber ->
            val client = getDefaultClient(context)
            val request = Request.Builder()
                    .url(url)
                    .cacheControl(CacheControl.Builder().maxAge(MAX_AGE_SECOND, TimeUnit.SECONDS).build())
                    .get()
                    .build();
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body().string()
                subscriber.onNext(json)
                subscriber.onCompleted()
            } else {
                subscriber.onError(ApiRequestException("error:" + response.code()));
            }
        }
    }

    fun get(context: Context, url: String): JSONObject {
        val client = getDefaultClient(context)
        val request = Request.Builder()
                .url(url)
                .cacheControl(CacheControl.Builder().maxAge(MAX_AGE_SECOND, TimeUnit.SECONDS).build())
                .get()
                .build();
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            return JSONObject(response.body().string())
        } else {
            throw ApiRequestException("error:" + response.code());
        }
    }

    fun oAuthRequest(account: Account, request: OAuthRequest): Response {
        val oAuthService = ServiceBuilder().provider(HatenaOAuthProvider::class.java).apiKey(Consumer.K).apiSecret(Consumer.S).build()
        val accessToken = Token(account.token, account.tokenSecret)
        oAuthService.signRequest(accessToken, request)
        return request.send()
    }

    @Synchronized fun getDefaultClient(context: Context): OkHttpClient {
        if (defaultClient != null) {
            return defaultClient!!
        }
        defaultClient = newClient(context)
        return defaultClient as OkHttpClient
    }

    private fun newClient(context: Context): OkHttpClient {
        val client = OkHttpClient().newBuilder()
        val cache = Cache(File(context.cacheDir, "mitsumine_cache"), MAX_CACHE_SIZE)
        client.cache(cache)
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BASIC
            client.addInterceptor(logging)
        }
        client.networkInterceptors().add(Interceptor { chain ->
            chain.proceed(chain.request()).newBuilder()
                    .header("cache-control", "max-age=" + MAX_AGE_SECOND)
                    .removeHeader("Pragma")
                    .build()
        })
        return client.build()
    }
}