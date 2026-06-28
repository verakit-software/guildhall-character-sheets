package com.eliasrvjimenez.guildhall.network

import com.apollographql.apollo.ApolloClient

/**
 * A provider for the ApolloClient to handle GraphQL operations.
 */
class ApolloClientProvider(private val serverUrl: String) {
    
    val apolloClient: ApolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl(serverUrl)
            .build()
    }
}
