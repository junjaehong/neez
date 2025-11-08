package com.bbey.neez.repository;

import com.bbey.neez.entity.BizCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BizCardRepository extends JpaRepository<BizCard, Long> {

    // 이름 + 이메일로 중복 체크
    Optional<BizCard> findByNameAndEmail(String name, String email);

    // 페이징 + 소프트 삭제 제외
    Page<BizCard> findByUserIdxAndIsDeletedFalse(Long userIdx, Pageable pageable);

    // 검색 (이름/이메일/부서) + 소프트 삭제 제외
    @Query("select b from BizCard b " +
            "where b.userIdx = :userIdx " +
            "and b.isDeleted = false " +
            "and (" +
            "   lower(b.name) like lower(concat('%', :keyword, '%')) or " +
            "   lower(b.email) like lower(concat('%', :keyword, '%')) or " +
            "   lower(b.department) like lower(concat('%', :keyword, '%'))" +
            ")")
    Page<BizCard> searchByKeyword(@Param("userIdx") Long userIdx, @Param("keyword") String keyword, Pageable pageable);
    
}
