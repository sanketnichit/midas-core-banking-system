package com.sanket.midas.repository;

import com.sanket.midas.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent>
    findByPublishedAtIsNullOrderByCreatedAtAsc();
}