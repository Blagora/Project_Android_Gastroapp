package com.example.gastroapp.data.remote

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class OverpassService @Inject constructor(
    private val requestQueue: RequestQueue
) {
    suspend fun buscarRestaurantes(bbox: String): String = suspendCancellableCoroutine { continuation ->
        val url = "https://overpass-api.de/api/interpreter"
        val query = """
            [out:json][timeout:25];
            (
              way["amenity"="restaurant"]($bbox);
              node["amenity"="restaurant"]($bbox);
            );
            out body;
            >;
            out skel qt;
        """.trimIndent()

        val request = object : StringRequest(
            Method.POST,
            url,
            Response.Listener { response ->
                continuation.resume(response)
            },
            Response.ErrorListener { error ->
                continuation.resumeWithException(error)
            }
        ) {
            override fun getBody(): ByteArray {
                return query.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Content-Type" to "application/x-www-form-urlencoded"
                )
            }
        }

        request.retryPolicy = DefaultRetryPolicy(
            15000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(request)

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }
}