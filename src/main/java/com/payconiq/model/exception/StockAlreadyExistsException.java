package com.payconiq.model.exception;

public class StockAlreadyExistsException extends RuntimeException {

    public StockAlreadyExistsException(final Long id) {
        super("Stock already exists for " + id);
    }

}
