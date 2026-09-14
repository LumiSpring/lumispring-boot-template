package com.lumispring.framework.base.config

import com.lumispring.framework.base.extension.*
import jakarta.annotation.PreDestroy
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpClientConfiguration {

    @Bean("iHttpClient")
    fun httpClient(): CloseableHttpClient {
        return createHttpClient()
    }

    @Bean("iHttpAsyncClient")
    fun httpAsyncClient(): CloseableHttpAsyncClient {
        return createHttpAsyncClient()
    }

    @Bean
    fun poolingHttpClientConnectionManager(): PoolingHttpClientConnectionManager {
        return createHttpPoolConnectionManager()
    }

    @Bean
    fun poolingAsyncClientConnectionManager():PoolingAsyncClientConnectionManager{
        return createHttpPoolAsyncConnectionManager()
    }

    @PreDestroy
    fun closeHttpClient(){
        try {
            httpClient().close()
            httpAsyncClient().close()
            poolingHttpClientConnectionManager().close()
            poolingAsyncClientConnectionManager().close()
        } catch (e: Exception) {
            logError("httpclient close fail")
        }
    }
}