package com.payconiq.service;

import com.payconiq.model.Stock;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class StockGenerator {

    public Map<Long, Stock> generateStocks(long count) {
        Map<Long, Stock> stockMap = new HashMap<>();
        while (count > 0) {
            Stock stock = Stock.builder()
                    .id(count)
                    .name("name_"+count)
                    .currentPrice(BigDecimal.valueOf(count+100000.66))
                    .timestamp(System.currentTimeMillis()).build();
            stockMap.put(stock.getId(), stock);
            count--;
        }
        return stockMap;
    }

}
