package com.payconiq.model;

import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRequest {

    @NotNull
    @Pattern(regexp = "^\\w+$", message = "{javax.validation.constraints.Pattern.alphanumeric.message}")
    private String name;

    @NotNull
    @Digits(integer = 12, fraction = 2, message = "{javax.validation.constraints.Digits.decimal.message}")
    @DecimalMin(value = "0", message = "{javax.validation.constraints.Digits.positive}")
    private BigDecimal currentPrice;

}
