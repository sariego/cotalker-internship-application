package com.cotalker.internship_application

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.text.Spanned
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    lateinit var reqQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        reqQueue = Volley.newRequestQueue(applicationContext)

        input1.filters = arrayOf(object : InputFilter {
            override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                try {
                    val input = Integer.parseInt(dest.toString() + source.toString())
                    if (input in 0..100) return null
                } catch (nfe: NumberFormatException) { }

                return ""
            }
        })

        button1.setOnClickListener {
            val req = ApiRequest(
                    Request.Method.POST,
                    "https://s8wojkby0k.execute-api.sa-east-1.amazonaws.com/prod/practica",
                    JSONObject(mapOf("length" to input1.text.toString())),
                    Response.Listener<JSONArray> { response ->
                        input2.setText((0 until response.length()).map { response.getString(it) }.reduce { acc, s -> acc + s })
                    },
                    Response.ErrorListener { error ->
                        error.networkResponse?.let {
                            val response = ApiRequest.parseErrorResponse(error.networkResponse)
                            if (response.has("errorMessage")) Toast.makeText(this, "ERROR: ${response.getString("errorMessage")}", Toast.LENGTH_SHORT).show()
                            else Toast.makeText(this, "UNHANDLED ERROR", Toast.LENGTH_SHORT).show()
                        }
                    }
            )
            reqQueue.add(req)
        }

        button2.setOnClickListener {
            output.text = replaceAsterisks(input2.text.toString()).joinToString(", ")
        }
    }

    fun replaceAsterisks(target: String): List<String> {
        if ('*' !in target) {
            return listOf(target)
        } else {
            val out = ArrayList<String>()
            out.addAll(replaceAsterisks(target.split('*', limit = 2).joinToString("0")))
            out.addAll(replaceAsterisks(target.split('*', limit = 2).joinToString("1")))
            return out
        }
    }

    class ApiRequest(
            method: Int,
            url: String,
            jsonRequest: JSONObject?,
            listener: Response.Listener<JSONArray>,
            errorListener: Response.ErrorListener
    ) : JsonRequest<JSONArray>(method, url, jsonRequest?.toString(), listener, errorListener) {

        override fun parseNetworkResponse(response: NetworkResponse): Response<JSONArray> {
            try {
                val jsonString = String(response.data,
                        Charset.forName(HttpHeaderParser.parseCharset(response.headers, JsonRequest.PROTOCOL_CHARSET)))
                return Response.success(JSONArray(jsonString),
                        HttpHeaderParser.parseCacheHeaders(response))
            } catch (e: UnsupportedEncodingException) {
                return Response.error(VolleyError(response))
            } catch (je: JSONException) {
                return Response.error(VolleyError(response))
            }
        }

        companion object {
            fun parseErrorResponse(response: NetworkResponse): JSONObject {
                try {
                    val jsonString = String(response.data,
                            Charset.forName(HttpHeaderParser.parseCharset(response.headers, JsonRequest.PROTOCOL_CHARSET)))
                    return JSONObject(jsonString)
                } catch (e: UnsupportedEncodingException) {
                    return JSONObject(mapOf("errorMessage" to e.localizedMessage))
                } catch (je: JSONException) {
                    return JSONObject(mapOf("errorMessage" to je.localizedMessage))
                }

            }
        }
    }
}
