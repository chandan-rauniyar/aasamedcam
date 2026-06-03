package com.aasemedem.demo.repository;

import com.aasemedem.demo.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** All orders for a specific user */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /** Admin: filter by status */
    Page<Order> findByStatus(String status, Pageable pageable);

    /** Admin: count by status */
    long countByStatus(String status);

    /** Order belonging to a user — prevents cross-user access */
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.user.id = :userId")
    java.util.Optional<Order> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}

