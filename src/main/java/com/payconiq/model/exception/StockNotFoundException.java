package com.payconiq.model.exception;

public class StockNotFoundException extends RuntimeException {

    public StockNotFoundException(final Long id) {
        super("Stock not found by " + id);
    }

}
