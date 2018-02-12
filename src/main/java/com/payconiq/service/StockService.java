package com.payconiq.service;

import com.payconiq.model.StockRequest;
import com.payconiq.model.StockResponse;
import com.payconiq.model.exception.StockNotFoundException;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Storage for stocks that handles create, update, get and list of stocks request
 */
@Service
public class StockService {

    private final ConcurrentHashMap<Long, Stock> stockMap = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);
    @Value("${stock-api.init-size:10}")
    private int initSize;

    /**
     * Initialize some stocks
     */
    @PostConstruct
    public void init() {
        for (long count=1; count <= initSize; count++) {
            final Stock stock = Stock.builder()
                    .id(idSequence.incrementAndGet())
                    .name("name_"+count)
                    .currentPrice(BigDecimal.valueOf(count+.66).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .lastUpdate(System.currentTimeMillis()).build();
            stockMap.put(stock.getId(), stock);
        }
    }

    public List<StockResponse> getStocks() {
        return stockMap.values().stream()
                .map(this::getStockResponse)
                .collect(Collectors.toList());
    }

    public StockResponse getStock(final Long id) {
        final Stock stock = stockMap.get(id);
        if (stock == null) {
            throw new StockNotFoundException(id);
        }
        return getStockResponse(stock);
    }

    public StockResponse createStock(final StockRequest stockRequest) {
        final Stock stock = createStockFromRequest(stockRequest, idSequence.incrementAndGet());
        stockMap.put(stock.getId(), stock);
        return getStockResponse(stock);
    }

    public StockResponse updateStock(final StockRequest stockRequest, final Long stockId) {
        if (!stockMap.containsKey(stockId)) {
            throw new StockNotFoundException(stockId);
        }
        final Stock stockToBeUpdated = createStockFromRequest(stockRequest, stockId);
        //Be aware of the fact that it could have been updated by another thread hopefully in normal databases
        //we can use versioning(OptimisticLocking) to control
        stockMap.put(stockId, stockToBeUpdated);
        return getStockResponse(stockToBeUpdated);
    }

    private Stock createStockFromRequest(final StockRequest stockRequest, final long stockId) {
        return Stock.builder()
                .id(stockId)
                .name(stockRequest.getName())
                .currentPrice(stockRequest.getCurrentPrice())
                .lastUpdate(System.currentTimeMillis())
                .build();
    }

    private StockResponse getStockResponse(final Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .name(stock.getName())
                .currentPrice(stock.getCurrentPrice())
                .lastUpdate(stock.getLastUpdate()).build();
    }

}

/**
 * Lets encapsulate the resource object and make it immutable
 */
@Data
@Builder
class Stock {
    @Setter(AccessLevel.NONE)
    private Long id;
    @Setter(AccessLevel.NONE)
    private String name;
    @Setter(AccessLevel.NONE)
    private BigDecimal currentPrice;
    @Setter(AccessLevel.NONE)
    private Long lastUpdate;
}