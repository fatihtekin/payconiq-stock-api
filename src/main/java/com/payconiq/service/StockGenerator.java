package com.payconiq.service;

import com.payconiq.model.Stock;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class StockGenerator {

    public Map<Long, Stock> generateStocks(long count) {
        Map<Long, Stock> stockMap = new HashMap<>();
        Random random = new Random();
        while (count --> 0) {
            Stock stock = Stock.builder()
                    .id(count)
                    .name("name_"+count)
                    .currentPrice(BigDecimal.valueOf(count+666.66))
                    .timestamp(System.currentTimeMillis()).build();
            stockMap.put(stock.getId(), stock);
        }
        return stockMap;
    }

}
