package com.rn00n.inhibitor.application.evnet.config

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class AsyncConfig : AsyncConfigurer {

    @Bean(name = ["eventTaskExecutor"])
    fun eventTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2 // 최소 스레드 수
        executor.maxPoolSize = 10 // 최대 스레드 수
        executor.queueCapacity = 100 // 작업 큐 크기
        executor.setThreadNamePrefix("event-async-") // 스레드 이름 접두어
        executor.initialize() // 스레드 풀 초기화
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler? {
        return MdcAwareAsyncExceptionHandler()
    }
}
