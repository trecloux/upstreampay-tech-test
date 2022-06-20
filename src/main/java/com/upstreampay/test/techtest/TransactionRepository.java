package com.upstreampay.test.techtest;

import com.upstreampay.test.techtest.domain.Transaction;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TransactionRepository extends ReactiveCrudRepository<Transaction, UUID> {
}
