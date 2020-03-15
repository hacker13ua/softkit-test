package org.ysurovskyi.softkittest.services;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.ysurovskyi.softkittest.domain.Company;
import org.ysurovskyi.softkittest.repository.CompanyRepository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResultService {
    public static final PageRequest TOP5_PAGE_REQUEST = PageRequest.of(0, 5, Sort.by(Sort.Order.asc("latestPrice").nullsLast(), Sort.Order.asc("companyName")));
    public static final String TOP_5_QUERY = "select symbol, abs(diff) as diff\n" +
            "from (select *,\n" +
            "             row_number() over (partition by symbol order by latest_update desc)                        as r,\n" +
            "             LAG(latest_price, 1) over (partition by symbol order by latest_update desc) - latest_price as diff\n" +
            "      from company_aud) as t\n" +
            "where r < 3 and diff is not null\n" +
            "order by diff desc limit 5";
    private final CompanyRepository companyRepository;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(initialDelay = 5_000, fixedDelay = 5_000)
    private void printTop5HighestValueStock() {
        Page<Company> latestPrice = companyRepository.findAll(TOP5_PAGE_REQUEST);
        log.info("TOP 5 highest value stock");
        latestPrice.get().forEach(company -> log.info("{}", company));
    }

    @Scheduled(initialDelay = 5_000, fixedDelay = 5_000)
    private void print5MostChangeable() {
        List<CompanyDiff> companyDiffs = jdbcTemplate.query(TOP_5_QUERY, (rs, rowNum) -> CompanyDiff.builder()
                .symbol(rs.getString("symbol"))
                .diff(rs.getBigDecimal("diff"))
                .build());
        log.info("TOP 5 diff");
        companyDiffs.forEach(company -> log.info("{}", company));
    }

    @Data
    @Builder
    private static class CompanyDiff {
        private String symbol;
        private BigDecimal diff;
    }
}
