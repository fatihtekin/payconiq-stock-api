package com.payconiq.endpoint;

import com.payconiq.model.StockRequest;
import com.payconiq.model.StockResponse;
import com.payconiq.model.exception.StockNotFoundException;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import com.payconiq.service.StockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * Common class for all the rest endpoint /api/stock interactions
 */
@Api("Payconiq Stock API")
@RestController
@RequestMapping(value = "/api/stocks")
public class StockController {

    @Autowired
    private StockService stockService;

    /**
     * Get stock by id
     * @param id of the Stock
     * @return Stock that is found by id
     */
    @ApiOperation(value = "Return stock by id")
    @GetMapping(value = "/{id}", produces = "application/json")
    public StockResponse getStock(@PathVariable final Long id) {
        return stockService.createStockFromRequest(id);
    }

    /**
     * Probably pagination could have been added if using in-memory or persistent database
     * for now keeping it simple
     * @return All the stocks
     */
    @ApiOperation(value = "Return the stocks")
    @GetMapping(produces = "application/json")
    public List<StockResponse> getStock() {
       return stockService.getStocks();
    }

    /**
     * Create stock atomically if not exists otherwise return error
     * @param stock to be created
     * @return created Stock
     */
    @ApiOperation(value = "Create stock")
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<StockResponse> createStock(@Valid @RequestBody final StockRequest stock) {
        final StockResponse stockResponse = stockService.createStock(stock);
        return new ResponseEntity<>(stockResponse, addLocationHeaders(stockResponse.getId()), HttpStatus.CREATED);
    }

    /**
     * Update stockRequest if exists otherwise return error
     * @param stockRequest to be Updated
     * @return updated Stock
     */
    @ApiOperation(value = "Update existing stock")
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> updateStock(@PathVariable final Long id, @Valid @RequestBody final StockRequest stockRequest) {
        stockService.updateStock(stockRequest, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Adding location to the HttpHeaders using Spring hateoas library
     * @param stockId of the stock
     * @return headers with locations
     */
    private HttpHeaders addLocationHeaders(final Long stockId) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(StockController.class).slash(stockId).toUri());
        return headers;
    }

    /**
     * Catch the StockNotFoundException error and then convert it into more generic httpResponse
     */
    @ExceptionHandler(StockNotFoundException.class)
    public void stockNotFoundHandler(final HttpServletResponse response, final StockNotFoundException exception) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
    }

}
