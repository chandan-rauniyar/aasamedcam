package com.aasemedem.demo.repository;

import com.aasemedem.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndIsActiveTrue(Long id);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    /** Search by name or category, active only */
    @Query("""
        SELECT p FROM Product p
        WHERE p.isActive = true
          AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.category) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<Product> search(@Param("q") String query, Pageable pageable);

    /** Filter by category */
    Page<Product> findByCategoryAndIsActiveTrue(String category, Pageable pageable);

    /** All distinct categories for filter dropdown */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.isActive = true AND p.category IS NOT NULL")
    List<String> findDistinctCategories();

    boolean existsBySku(String sku);
}

