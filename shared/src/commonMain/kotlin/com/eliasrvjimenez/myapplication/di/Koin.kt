package com.eliasrvjimenez.myapplication.di

import com.eliasrvjimenez.myapplication.ViewModel.AppViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.KoinAppDeclaration


class DiTestObject {
    fun greet() = "Koin is working!"
}

val commonModule = module {
    single { DiTestObject() }

    viewModelOf(::AppViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(commonModule)
}
