package com.payconiq.service;

import com.payconiq.model.StockRequest;
import com.payconiq.model.StockResponse;
import com.payconiq.model.exception.StockNotFoundException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.List;

/**
 * Check in memory storage service {@link StockService}
 */
public class StockServiceTest {

    private static final StockService stockService = new StockService();

    @BeforeClass
    public static void setup() {
        ReflectionTestUtils.setField(stockService, "initSize", 10);
        stockService.init();
        final List<StockResponse> responses = stockService.getStocks();
        Assert.assertEquals("Initial stocks count not correct", 10, responses.size());
    }

    @Test
    public void test_Given_InitialStocksCreated_Then_GetStockSuccessfully() {
        final StockResponse stockResponse = stockService.createStockFromRequest(1L);
        Assert.assertNotNull(stockResponse.getLastUpdate());
        final StockResponse expectedStockResponse = StockResponse.builder()
                .id(1L)
                .name("name_1")
                .currentPrice(new BigDecimal("1.66"))
                .lastUpdate(stockResponse.getLastUpdate()).build();
        Assert.assertEquals("Get stock is not working", expectedStockResponse, stockResponse);
    }

    @Test
    public void test_Given_ValidStockUpdateRequest_Then_UpdateStockSuccessfully() throws InterruptedException {
        final BigDecimal currentPrice = new BigDecimal("666.6");
        final String name = "testUpdated";
        final Long lastUpdateMax = System.currentTimeMillis();
        Thread.sleep(50L);
        final StockRequest stockRequest = StockRequest.builder().name(name).currentPrice(currentPrice).build();
        final long stockId = 2L;
        final StockResponse stockResponse = stockService.updateStock(stockRequest, stockId);
        Assert.assertTrue("lastUpdate is not updated to later date", stockResponse.getLastUpdate() > lastUpdateMax);
        Assert.assertEquals("currentPrice is wrong", currentPrice, stockResponse.getCurrentPrice());
        Assert.assertEquals("name is wrong", name, stockResponse.getName());
        Assert.assertEquals("id is wrong", Long.valueOf(stockId), stockResponse.getId());
        Assert.assertEquals("Stock is not stored stored properly", stockResponse, stockService.createStockFromRequest(stockResponse.getId()));
    }

    @Test
    public void test_Given_ValidStockCreateRequest_Then_CreateStockSuccessfully() {
        final BigDecimal currentPrice = new BigDecimal("12.45");
        final String name = "test";
        final StockRequest stockRequest = StockRequest.builder().name(name).currentPrice(currentPrice).build();
        final StockResponse stockResponse = stockService.createStock(stockRequest);
        Assert.assertNotNull("lastUpdate should not be null", stockResponse.getLastUpdate());
        Assert.assertNotNull("id should not be null", stockResponse.getId());
        Assert.assertEquals("currentPrice is wrong", currentPrice, stockResponse.getCurrentPrice());
        Assert.assertEquals("name is wrong", name, stockResponse.getName());
        Assert.assertEquals("Stock is not stored stored properly", stockResponse, stockService.createStockFromRequest(stockResponse.getId()));
    }

    @Test(expected = StockNotFoundException.class)
    public void test_Given_NonExistingStockUpdateRequest_Then_UpdateShouldFail() {
        final BigDecimal currentPrice = new BigDecimal("12.45");
        final String name = "test";
        final StockRequest stockRequest = StockRequest.builder().name(name).currentPrice(currentPrice).build();
        stockService.updateStock(stockRequest, -666L);
    }

}
