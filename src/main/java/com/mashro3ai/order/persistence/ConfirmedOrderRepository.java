package com.mashro3ai.order.persistence;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ConfirmedOrderRepository extends JpaRepository<ConfirmedOrder, UUID> {
    Optional<ConfirmedOrder> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<ConfirmedOrder> findByBusinessId(UUID businessId, Pageable pageable);
}
