package com.payconiq.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.payconiq.model.StockRequest;
import com.payconiq.model.StockResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("file:./application.yml")
@RunWith(SpringJUnit4ClassRunner.class)
public class StockControllerTest {

    private static List<Long> ONE_TO_TEN;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${stock-api.init-size:10}")
    private int initSize;
    @Value("${local.server.port}")
    private int port;
    @Value("${endpoints.prometheus.path}")
    private String urlPath;
    private final MapType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
    private String metricsUrl;

    @Before
    public void init() {
        restTemplate = restTemplate.withBasicAuth("admin", "admin");
        ONE_TO_TEN = LongStream.rangeClosed(1, initSize).boxed().collect(Collectors.toList());
        metricsUrl = "http://localhost:" + port + "/"+ urlPath;
    }

    @Test
    public void test_GivenStocksInit_WhenGetStocks_ThenReturnStocksSuccessfully() {
        final ResponseEntity<StockResponse[]> responseEntity = restTemplate
                .getForEntity("/api/stocks", StockResponse[].class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().length >= 10);
        assertTrue(Arrays.stream(responseEntity.getBody()).map(StockResponse::getId).collect(Collectors.toList()).containsAll(ONE_TO_TEN));
        testMetric("/api/stocks", "GET", HttpStatus.OK);
    }

    @Test
    public void test_WhenGetStock_ThenReturnStockSuccessfully() {
        final ResponseEntity<StockResponse> responseEntity = restTemplate
                .getForEntity("/api/stocks/1", StockResponse.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        final StockResponse stockResponse = responseEntity.getBody();
        Assert.assertNotNull(stockResponse.getLastUpdate());
        final StockResponse expectedStockResponse = StockResponse.builder()
                .id(1L)
                .name("name_1")
                .currentPrice(new BigDecimal("100001.66"))
                .lastUpdate(stockResponse.getLastUpdate()).build();
        Assert.assertEquals("Get stock is not working", expectedStockResponse, stockResponse);
        testMetric("/api/stocks/*", "GET", HttpStatus.OK);
    }

    @Test
    public void test_WhenNonExistenceGetStock_ThenFail() throws IOException {
        final ResponseEntity<String> errorEntity = restTemplate
                .getForEntity("/api/stocks/-123", String.class);
        assertEquals(HttpStatus.NOT_FOUND, errorEntity.getStatusCode());
        final Map<String, String> errorMap = objectMapper.readValue(errorEntity.getBody(), mapType);
        assertEquals("Error message in body", "Stock not found by -123", errorMap.get("message"));
        testMetric("/api/stocks/*", "GET", HttpStatus.NOT_FOUND);
    }

    @Test
    public void test_WhenInvalidGetStock_ThenFail() {
        final ResponseEntity<String> errorEntity = restTemplate
                .getForEntity("/api/stocks/abc", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, errorEntity.getStatusCode());
        testMetric("/api/stocks/*", "GET", HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_WhenValidCreateStock_ThenReturnStockSuccessfully() {
        final BigDecimal currentPrice = new BigDecimal("12.45");
        final String name = "test";
        final StockRequest stockRequest = StockRequest.builder().name(name).currentPrice(currentPrice).build();
        final ResponseEntity<StockResponse> stockResponse = restTemplate.postForEntity("/api/stocks", stockRequest, StockResponse.class);
        assertEquals(HttpStatus.CREATED, stockResponse.getStatusCode());
        assertEquals(String.format("http://localhost:%d/api/stocks/11", port), stockResponse.getHeaders().get(HttpHeaders.LOCATION).get(0));
        testMetric("/api/stocks", "POST", HttpStatus.CREATED);
    }

    @Test
    public void test_WhenInValidCreateStock_ThenFail() {
        final BigDecimal currentPrice = new BigDecimal("12.45");
        final StockRequest stockRequest = StockRequest.builder().currentPrice(currentPrice).build();
        final ResponseEntity<StockResponse> stockResponse = restTemplate.postForEntity("/api/stocks", stockRequest, StockResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, stockResponse.getStatusCode());
        testMetric("/api/stocks", "POST", HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_WhenInValidJsonCreateStock_ThenFail() {
        final String invalidJson = "{id\":\"2\"}";
        final ResponseEntity<String> stockResponse = restTemplate.postForEntity("/api/stocks", invalidJson, String.class);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, stockResponse.getStatusCode());
        testMetric("/api/stocks", "POST", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void test_WhenValidUpdateStock_ThenReturnStockSuccessfully() {
        final BigDecimal currentPrice = new BigDecimal("666.6");
        final String name = "testUpdated";
        final StockRequest stockRequest = StockRequest.builder().name(name).currentPrice(currentPrice).build();
        final ResponseEntity<Void> stockResponse = restTemplate.exchange("/api/stocks/2", HttpMethod.PUT, new HttpEntity(stockRequest), Void.class, new HashMap<>());
        assertEquals(HttpStatus.NO_CONTENT, stockResponse.getStatusCode());
        testMetric("/api/stocks/*", "PUT", HttpStatus.NO_CONTENT);
    }

    @Test
    public void test_WhenNonExistenceUpdateStock_ThenFail() {
        final BigDecimal currentPrice = new BigDecimal("666.6");
        final String name = "testUpdated";
        final StockRequest stockRequest = StockRequest.builder().name(name).currentPrice(currentPrice).build();
        final ResponseEntity<Void> stockResponse = restTemplate.exchange("/api/stocks/-222", HttpMethod.PUT, new HttpEntity(stockRequest), Void.class, new HashMap<>());
        assertEquals(HttpStatus.NOT_FOUND, stockResponse.getStatusCode());
        testMetric("/api/stocks/*", "PUT", HttpStatus.NOT_FOUND);
    }

    private void testMetric(final String path, final String method, final HttpStatus status) {
        final ResponseEntity<String> metricsResponse = restTemplate.getForEntity(metricsUrl, String.class);
        assertEquals("Expected HTTP OK response", HttpStatus.OK, metricsResponse.getStatusCode());
        final String metricsData = metricsResponse.getBody();
        final String metricName = String.format("http_request_duration_seconds_bucket{method=\"%s\",path=\"%s\",status=\"%d\",le=\"1.0\",} 1.0",
                method, path, status.value());
        assertTrue(String.format("%s value incorrect.\nMetrics: %s", metricName, metricsData), metricsData.contains(metricName));
    }
}
