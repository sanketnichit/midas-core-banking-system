package com.sanket.midas.repository;

import com.sanket.midas.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    Optional<Reward> findByTransactionId(Long transactionId);

    List<Reward> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}