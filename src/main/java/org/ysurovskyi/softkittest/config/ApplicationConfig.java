package org.ysurovskyi.softkittest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import pl.zankowski.iextrading4j.client.IEXCloudClient;
import pl.zankowski.iextrading4j.client.IEXCloudTokenBuilder;
import pl.zankowski.iextrading4j.client.IEXTradingApiVersion;
import pl.zankowski.iextrading4j.client.IEXTradingClient;

import java.util.concurrent.ForkJoinPool;

@Configuration
public class ApplicationConfig {
    @Bean
    public IEXCloudClient iexTradingClient() {
        return IEXTradingClient.create(IEXTradingApiVersion.IEX_CLOUD_V1_SANDBOX,
                new IEXCloudTokenBuilder()
                        .withPublishableToken("Tpk_ee567917a6b640bb8602834c9d30e571")
                        .build());
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(@Value("${corePoolSize:16}") int corePoolSize) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(corePoolSize);
        return threadPoolTaskScheduler;
    }

    @Bean
    public ForkJoinPool companiesPool(@Value("${companiesPoolSize:4}") int poolSize) {
        return new ForkJoinPool(poolSize);
    }
}
