package com.upstreampay.test.techtest.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class OrderLine {
  @NotBlank
  private String productName;
  @Positive
  private Integer quantity;
  @Positive
  private Long unitPrice;
}
