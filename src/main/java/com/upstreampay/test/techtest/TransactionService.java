package com.upstreampay.test.techtest;

import static com.upstreampay.test.techtest.domain.TransactionStatus.AUTHORIZED;
import static com.upstreampay.test.techtest.domain.TransactionStatus.CAPTURED;
import static com.upstreampay.test.techtest.domain.TransactionStatus.NEW;

import com.upstreampay.test.techtest.domain.Transaction;
import com.upstreampay.test.techtest.domain.TransactionStatus;
import com.upstreampay.test.techtest.misc.BadRequestException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TransactionService {

  final TransactionRepository repository;

  public Mono<Transaction> create(Transaction tx) {
    return repository.save(tx.toBuilder().status(NEW).id(UUID.randomUUID()).build());
  }

  public Flux<Transaction> findAll() {
    return repository.findAll();
  }

  public Mono<Transaction> findById(UUID id) {
    return repository.findById(id);
  }

  public Mono<Transaction> changeStatus(UUID id, TransactionStatus status) {
    return findById(id).flatMap(tx -> {
      if (CAPTURED.equals(status) && ! AUTHORIZED.equals(tx.getStatus())) {
        return Mono.error(new BadRequestException("Can't change status from " + tx.getStatus() + " to " +status));
      }
      if (CAPTURED.equals(tx.getStatus())) {
        return Mono.error(new BadRequestException("Can't change status from " + tx.getStatus() + " to " +status));
      }

      return repository.save(tx.toBuilder().status(status).build());
    });
  }
}
