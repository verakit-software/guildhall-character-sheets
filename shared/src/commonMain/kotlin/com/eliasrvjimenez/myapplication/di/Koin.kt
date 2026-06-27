package com.eliasrvjimenez.myapplication.di

import com.eliasrvjimenez.myapplication.network.ApiClient
import com.eliasrvjimenez.myapplication.network.ApolloClientProvider
import com.eliasrvjimenez.myapplication.viewmodel.AppViewModel
import com.eliasrvjimenez.myapplication.viewmodel.wiki.WikiViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


class DiTestObject {
    fun greet() = "Koin is working!"
}

val networkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }

    single { ApiClient(get()) }

    single { 
        // Replace with your actual DND API GraphQL endpoint
        ApolloClientProvider("https://www.dnd5eapi.co/graphql").apolloClient
    }
}

val commonModule = module {
    includes(networkModule)
    single { DiTestObject() }

    viewModelOf(::AppViewModel)
    viewModelOf(::WikiViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(commonModule)
}
