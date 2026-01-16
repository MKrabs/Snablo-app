package de.mkrabs.snablo.app.di

import de.mkrabs.snablo.app.data.api.PocketBaseApi
import de.mkrabs.snablo.app.data.repository.AuthRepository
import de.mkrabs.snablo.app.data.repository.ProductRepository
import de.mkrabs.snablo.app.data.repository.TransactionRepository
import de.mkrabs.snablo.app.data.session.SessionManager
import de.mkrabs.snablo.app.viewmodel.AuthViewModel
import de.mkrabs.snablo.app.viewmodel.HomeViewModel
import de.mkrabs.snablo.app.viewmodel.AdminViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    // HTTP Client
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }
            install(Logging) {
                level = LogLevel.BODY
            }
        }
    }

    // Session
    singleOf(::SessionManager)

    // API
    singleOf(::PocketBaseApi)

    // Repositories
    singleOf(::AuthRepository)
    singleOf(::ProductRepository)
    singleOf(::TransactionRepository)

    // ViewModels
    factoryOf(::AuthViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::AdminViewModel)
}
