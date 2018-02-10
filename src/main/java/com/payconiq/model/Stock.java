package com.payconiq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Digits;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @NotNull
    @Min(value = 1L, message = "{javax.validation.constraints.positive}")
    private Long id;

    @NotNull
    @Pattern(regexp = "^\\w+$", message = "{javax.validation.constraints.Pattern.alphanumeric.message}")
    private String name;

    @NotNull
    @Digits(integer = 12, fraction = 2, message = "{javax.validation.constraints.Digits.decimal.message}")
    @DecimalMin(value = "0", message = "{javax.validation.constraints.Digits.positive}")
    private BigDecimal currentPrice;

    /**
     * Used epoch timestamp with miliseconds as no need to do calculation and also
     * it made more sense for timestamp field to be readonly and not updatable
     * to keep things simpler.
     */
    private Long timestamp;

}
