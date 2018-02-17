package com.payconiq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {

    private Long id;
    private String name;
    private BigDecimal currentPrice;
    /**
     * Used epoch lastUpdate with milliseconds as no need to do calculation and also
     * it made more sense for lastUpdate field to be readonly and not updatable
     * to keep things simpler.
     */
    private Long lastUpdate;

}
