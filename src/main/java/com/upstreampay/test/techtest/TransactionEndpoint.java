package com.upstreampay.test.techtest;

import com.upstreampay.test.techtest.domain.Transaction;
import com.upstreampay.test.techtest.domain.TransactionStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v0/transactions")
@RequiredArgsConstructor
public class TransactionEndpoint {

  final TransactionService service;

  @GetMapping
  public Flux<Transaction> findAll() {
    return service.findAll();
  }

  @GetMapping("/{id}")
  public Mono<Transaction> findById(@PathVariable UUID id) {
    return service.findById(id);
  }

  @PostMapping
  public Mono<Transaction> create(@RequestBody @Validated Transaction tx) {
    return service.create(tx);
  }

  @PutMapping("/{id}/status")
  public Mono<Transaction> changeStatus(@PathVariable UUID id, @RequestBody TransactionStatus status) {
    return service.changeStatus(id, status);
  }
}
