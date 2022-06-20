package com.upstreampay.test.techtest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class Transaction {
  @Id UUID id;
  @NotBlank String currency; // TODO use strong type
  @Positive Long totalAmount;
  @NotNull PaymentMethod paymentMethod;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  TransactionStatus status;

  @NotEmpty List<OrderLine> orderLines;
}
