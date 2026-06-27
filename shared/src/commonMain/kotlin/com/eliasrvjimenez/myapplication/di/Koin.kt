package com.eliasrvjimenez.myapplication.di

import com.eliasrvjimenez.myapplication.ViewModel.AppViewModel
import com.eliasrvjimenez.myapplication.network.ApiClient
import com.eliasrvjimenez.myapplication.network.ApolloClientProvider
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.KoinAppDeclaration


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
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(commonModule)
}
