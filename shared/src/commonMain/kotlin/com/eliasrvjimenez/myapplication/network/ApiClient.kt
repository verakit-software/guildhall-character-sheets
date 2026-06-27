package com.eliasrvjimenez.myapplication.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * A generic REST API client wrapper using Ktor.
 * This class handles standard HTTP operations and is future-proofed for 
 * POST, PUT, and DELETE calls.
 */
class ApiClient(val httpClient: HttpClient) {

    /**
     * Performs a GET request and returns the deserialized body.
     */
    suspend inline fun <reified T> get(endpoint: String): T {
        return httpClient.get(endpoint).body()
    }

    /**
     * Performs a POST request with an optional body and returns the deserialized result.
     */
    suspend inline fun <reified T, reified B> post(endpoint: String, body: B? = null): T {
        return httpClient.post(endpoint) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.body()
    }

    /**
     * Performs a PUT request with an optional body and returns the deserialized result.
     */
    suspend inline fun <reified T, reified B> put(endpoint: String, body: B? = null): T {
        return httpClient.put(endpoint) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.body()
    }

    /**
     * Performs a DELETE request and returns the deserialized result.
     */
    suspend inline fun <reified T> delete(endpoint: String): T {
        return httpClient.delete(endpoint).body()
    }
}
