package org.ysurovskyi.softkittest.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import pl.zankowski.iextrading4j.api.refdata.v1.ExchangeSymbol;
import pl.zankowski.iextrading4j.client.IEXCloudClient;
import pl.zankowski.iextrading4j.client.rest.manager.RestRequest;
import pl.zankowski.iextrading4j.client.rest.request.filter.RequestFilter;
import pl.zankowski.iextrading4j.client.rest.request.refdata.v1.SymbolsRequestBuilder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyLoader {
    private final IEXCloudClient iexTradingClient;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private ReentrantLock lock = new ReentrantLock();

    @Scheduled(initialDelay = 5_000, fixedDelay = 5_000)
    public void requestAllCompanies() {
        if (lock.tryLock()) {
            try {
                log.info("Request exchange symbols");
                final RestRequest<List<ExchangeSymbol>> request = new SymbolsRequestBuilder()
                        .withRequestFilter(RequestFilter.builder()
                                .withColumn("isEnabled")
                                .withColumn("symbol")
                                .build())
                        .build();
                List<ExchangeSymbol> exchangeSymbols = iexTradingClient.executeRequest(request).stream()
                        .filter(ExchangeSymbol::getEnabled)
                        .collect(Collectors.toList());
                log.info("Total loaded {} exchange symbols", exchangeSymbols.size());
                try {
                    CompanyTask companyTask = companyTask();
                    companyTask.setExchangeSymbols(exchangeSymbols);
                    threadPoolTaskScheduler
                            .submit(companyTask)
                            .get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error while process company task", e);
                }
            } finally {
                lock.unlock();
            }
        } else {
            log.info("Companies are processing");
        }
    }

    @Lookup
    CompanyTask companyTask() {
        return null;
    }
}
