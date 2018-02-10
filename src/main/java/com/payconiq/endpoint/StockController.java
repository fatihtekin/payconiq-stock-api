package com.payconiq.endpoint;

import com.payconiq.model.Stock;
import com.payconiq.model.exception.StockAlreadyExistsException;
import com.payconiq.model.exception.StockNotFoundException;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import com.payconiq.service.StockGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping(value = "/api/stocks")
public class StockController {

    private final ConcurrentHashMap<Long, Stock> stockMap = new ConcurrentHashMap<>();
    @Autowired
    private StockGenerator stockGenerator;

    @PostConstruct
    public void init() {
        stockMap.putAll(stockGenerator.generateStocks(10));
    }

    /**
     *
     * @param id
     * @return
     * @throws StockNotFoundException
     */
    @GetMapping(value = "/{id}", produces = "application/json")
    public Stock getStock(@PathVariable final Long id) {
        final Stock stock = stockMap.get(id);
        if (stock == null) {
            throw new StockNotFoundException(id);
        }
        return stock;
    }

    @GetMapping(produces = "application/json")
    public Collection<Stock> getStock() {
       return stockMap.values();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Stock> createStock(@Valid @RequestBody final Stock stock) {
        stock.setTimestamp(System.currentTimeMillis());
        //Lets keep atomicity by using putIfAbsent https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ConcurrentHashMap.html#putIfAbsent(K,%20V)
        if (stockMap.putIfAbsent(stock.getId(), stock) != null) {
            //That means another thread already called to insert the same or in short the same element already exists in the map
            throw new StockAlreadyExistsException(stock.getId());
        }
        return new ResponseEntity<>(stock, addLocationHeaders(stock), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Stock> updateStock(@Valid @RequestBody Stock stock) {
        final Stock existingStock = stockMap.get(stock.getId());
        if (existingStock == null) {
            throw new StockNotFoundException(stock.getId());
        }
        existingStock.setCurrentPrice(stock.getCurrentPrice());
        existingStock.setName(stock.getName());
        return new ResponseEntity<>(stock, addLocationHeaders(stock), HttpStatus.OK);
    }

    private HttpHeaders addLocationHeaders(Stock stock) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(StockController.class).slash(stock.getId()).toUri());
        return headers;
    }

    @ExceptionHandler(StockNotFoundException.class)
    public void segmentNotFoundHandler(final HttpServletResponse response,
                                       final StockNotFoundException exception) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(StockAlreadyExistsException.class)
    public void segmentAlreadyExistsHandler(final HttpServletResponse response,
                                       final StockAlreadyExistsException exception) throws IOException {
        response.sendError(HttpServletResponse.SC_CONFLICT, exception.getMessage());
    }

}
