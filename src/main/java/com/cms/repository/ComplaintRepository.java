package com.cms.repository;

import com.cms.entity.Complaint;
import com.cms.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByComplaintCode(String complaintCode);

    Boolean existsByComplaintCode(String complaintCode);

    Page<Complaint> findByUserId(Long userId, Pageable pageable);

    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, Status status);

    long countByStatus(Status status);

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:search IS NULL OR LOWER(c.complaintCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(c.location) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(c.user.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Complaint> findWithFilters(
            @Param("status") Status status,
            @Param("category") String category,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT c FROM Complaint c WHERE c.user.id = :userId AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:search IS NULL OR LOWER(c.complaintCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(c.location) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Complaint> findUserComplaintsWithFilters(
            @Param("userId") Long userId,
            @Param("status") Status status,
            @Param("category") String category,
            @Param("search") String search,
            Pageable pageable
    );
}
