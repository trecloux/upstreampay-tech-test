package com.upstreampay.test.techtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class TransactionIntegrationtest {

  private Logger logger = LoggerFactory.getLogger(getClass());


  // quick and dirty workaround for
  // https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/337
  static {
    System.setProperty("os.arch", "amd64");
  }

  final WebTestClient webTestClient;
  final TransactionRepository repository;

  public TransactionIntegrationtest(@Autowired WebTestClient webTestClient, @Autowired TransactionRepository repository) {
    this.webTestClient = webTestClient;
    this.repository = repository;
  }

  @BeforeEach
  public void cleanup() {
    repository.deleteAll().block();
  }

  @Test
  public void should_find_none() {
    webTestClient
        .get()
        .uri("/v0/transactions")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(0);
  }

  @Test
  public void smoke_test() {

    AtomicReference<String> id = new AtomicReference<>();
    webTestClient
        .post()
        .uri("/v0/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("""
          {
            "paymentMethod": "CREDIT_CARD",
            "currency": "EUR",
            "totalAmount": 5480,
            "orderLines": [
              {"productName": "Ski gloves", "quantity": 4, "unitPrice": 1000},
              {"productName": "Beany", "quantity": 1, "unitPrice": 1480}
            ]
          }
        """)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.id").value(id::set)
        .jsonPath("$.paymentMethod").isEqualTo("CREDIT_CARD")
        .jsonPath("$.status").isEqualTo("NEW")
        .jsonPath("$.currency").isEqualTo("EUR")
        .jsonPath("$.totalAmount").isEqualTo(5480)
        .jsonPath("$.orderLines.length()").isEqualTo(2)
        .jsonPath("$.orderLines[0].productName").isEqualTo("Ski gloves")
        .jsonPath("$.orderLines[0].quantity").isEqualTo(4)
        .jsonPath("$.orderLines[0].unitPrice").isEqualTo(1000)
        .jsonPath("$.orderLines[1].productName").isEqualTo("Beany")
        .jsonPath("$.orderLines[1].quantity").isEqualTo(1)
        .jsonPath("$.orderLines[1].unitPrice").isEqualTo(1480)
    ;

    webTestClient
        .get()
        .uri("/v0/transactions/{id}", id.get())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo(id.get())
        .jsonPath("$.status").isEqualTo("NEW")
        .jsonPath("$.paymentMethod").isEqualTo("CREDIT_CARD")
        .jsonPath("$.status").isEqualTo("NEW")
        .jsonPath("$.currency").isEqualTo("EUR")
        .jsonPath("$.totalAmount").isEqualTo(5480)
        .jsonPath("$.orderLines.length()").isEqualTo(2)
        .jsonPath("$.orderLines[0].productName").isEqualTo("Ski gloves")
        .jsonPath("$.orderLines[0].quantity").isEqualTo(4)
        .jsonPath("$.orderLines[0].unitPrice").isEqualTo(1000)
        .jsonPath("$.orderLines[1].productName").isEqualTo("Beany")
        .jsonPath("$.orderLines[1].quantity").isEqualTo(1)
        .jsonPath("$.orderLines[1].unitPrice").isEqualTo(1480)
    ;

    webTestClient
        .get()
        .uri("/v0/transactions")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].id").isEqualTo(id.get())
        .jsonPath("$[0].status").isEqualTo("NEW")
        .jsonPath("$[0].paymentMethod").isEqualTo("CREDIT_CARD")
        .jsonPath("$[0].status").isEqualTo("NEW")
        .jsonPath("$[0].currency").isEqualTo("EUR")
        .jsonPath("$[0].totalAmount").isEqualTo(5480)
        .jsonPath("$[0].orderLines.length()").isEqualTo(2)
        .jsonPath("$[0].orderLines[0].productName").isEqualTo("Ski gloves")
        .jsonPath("$[0].orderLines[0].quantity").isEqualTo(4)
        .jsonPath("$[0].orderLines[0].unitPrice").isEqualTo(1000)
        .jsonPath("$[0].orderLines[1].productName").isEqualTo("Beany")
        .jsonPath("$[0].orderLines[1].quantity").isEqualTo(1)
        .jsonPath("$[0].orderLines[1].unitPrice").isEqualTo(1480);

    update_status_expect_status(id.get(), "CAPTURED", BAD_REQUEST);
    assert_tx_status_is(id, "NEW");
    update_status_expect_status(id.get(), "AUTHORIZED", OK);
    assert_tx_status_is(id, "AUTHORIZED");
    update_status_expect_status(id.get(), "NEW", OK);
    assert_tx_status_is(id, "NEW");
    update_status_expect_status(id.get(), "AUTHORIZED", OK);
    assert_tx_status_is(id, "AUTHORIZED");
    update_status_expect_status(id.get(), "CAPTURED", OK);
    assert_tx_status_is(id, "CAPTURED");
    update_status_expect_status(id.get(), "AUTHORIZED", BAD_REQUEST);
    update_status_expect_status(id.get(), "NEW", BAD_REQUEST);
    assert_tx_status_is(id, "CAPTURED");

    webTestClient
      .post()
      .uri("/v0/transactions")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue("""
          {
            "paymentMethod": "PAYPAL",
            "currency": "EUR",
            "totalAmount": 20800,
            "orderLines": [
              {"productName": "Bike", "quantity": 1, "unitPrice": 20800}
            ]
          }
        """)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.id").isNotEmpty()
      .jsonPath("$.id").value(id::set)
      .jsonPath("$.paymentMethod").isEqualTo("PAYPAL")
      .jsonPath("$.status").isEqualTo("NEW")
      .jsonPath("$.currency").isEqualTo("EUR")
      .jsonPath("$.totalAmount").isEqualTo(20800)
      .jsonPath("$.orderLines.length()").isEqualTo(1)
      .jsonPath("$.orderLines[0].productName").isEqualTo("Bike")
      .jsonPath("$.orderLines[0].quantity").isEqualTo(1)
      .jsonPath("$.orderLines[0].unitPrice").isEqualTo(20800);

    webTestClient
      .get()
      .uri("/v0/transactions")
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.length()").isEqualTo(2);

  }

  private void assert_tx_status_is(AtomicReference<String> id, String expectedStatus) {
    webTestClient
      .get()
      .uri("/v0/transactions/{id}", id.get())
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.status").isEqualTo(expectedStatus);
  }

  private void update_status_expect_status(String id, String newTxStatus, HttpStatus expectedHttpStatus) {
    webTestClient
      .put()
      .uri("/v0/transactions/{id}/status", id)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue("""
        "%s"
        """.formatted(newTxStatus))
      .exchange()
      .expectStatus()
      .isEqualTo(expectedHttpStatus);
  }
}
