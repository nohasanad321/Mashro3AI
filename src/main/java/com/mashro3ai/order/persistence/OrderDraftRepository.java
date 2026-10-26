package com.mashro3ai.order.persistence;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderDraftRepository extends JpaRepository<OrderDraft, UUID> {
    Optional<OrderDraft> findByIdAndBusinessId(UUID id, UUID businessId);
    org.springframework.data.domain.Page<OrderDraft> findByBusinessId(UUID businessId, org.springframework.data.domain.Pageable pageable);
}
