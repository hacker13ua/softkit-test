package org.ysurovskyi.softkittest.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.ysurovskyi.softkittest.domain.Company;
import org.ysurovskyi.softkittest.repository.CompanyRepository;
import pl.zankowski.iextrading4j.api.refdata.v1.ExchangeSymbol;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import pl.zankowski.iextrading4j.client.IEXCloudClient;
import pl.zankowski.iextrading4j.client.rest.manager.RestRequest;
import pl.zankowski.iextrading4j.client.rest.request.stocks.QuoteRequestBuilder;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class CompanyTask implements Runnable {
    private final IEXCloudClient iexTradingClient;
    private final CompanyRepository companyRepository;
    private final ForkJoinPool companiesPool;

    private List<ExchangeSymbol> exchangeSymbols;

    private static Company mapToCompany(Quote quote) {
        return Company.builder()
                .symbol(quote.getSymbol())
                .companyName(quote.getCompanyName())
                .latestUpdate(quote.getLatestUpdate())
                .latestPrice(quote.getLatestPrice())
                .high(quote.getHigh())
                .low(quote.getLow())
                .build();
    }

    @Override
    public void run() {
        try {
            List<Company> collect = companiesPool.submit(
                    () -> exchangeSymbols.parallelStream()
                            .map(es -> new QuoteRequestBuilder()
                                    .withSymbol(es.getSymbol())
                                    .build())
                            .map(this::wrapRest)
                            .filter(Objects::nonNull)
                            .map(CompanyTask::mapToCompany)
                            .map(companyRepository::save)
                            .collect(Collectors.toList()))
                    .get();
            log.info("All companies collected and saved in db. size={}", collect.size());
        } catch (InterruptedException | ExecutionException e) {
            log.error("WTF", e);
        }
    }

    public void setExchangeSymbols(List<ExchangeSymbol> exchangeSymbols) {
        this.exchangeSymbols = exchangeSymbols;
    }

    private Quote wrapRest(RestRequest<Quote> restRequest) {
        try {
            return iexTradingClient.executeRequest(restRequest);
        } catch (Exception e) {
            log.error("Error while rest request ", e);
            return null;
        }
    }
}
