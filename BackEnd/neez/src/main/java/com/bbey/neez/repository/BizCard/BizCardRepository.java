package com.bbey.neez.repository.BizCard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbey.neez.entity.BizCard.BizCard;

import java.util.Optional;
import java.util.List;

public interface BizCardRepository extends JpaRepository<BizCard, Long> {

    // 이름 + 이메일로 중복 체크
    Optional<BizCard> findByNameAndEmail(String name, String email);

    // 추가: 사용자까지 포함해서 찾기
    Optional<BizCard> findByUserIdxAndNameAndEmail(Long userIdx, String name, String email);

    // 사용자 소유 명함 리스트 호출 + 페이징 + 소프트 삭제 제외
    Page<BizCard> findByUserIdxAndIsDeletedFalse(Long userIdx, Pageable pageable);

    // 사용자 소유 명함 리스트 호출 + 페이징 + 소프트 삭제 제외 + 최신 등록순    
    Page<BizCard> findByUserIdxAndIsDeletedFalseOrderByCreatedAtDesc(Long userIdx, Pageable pageable);


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

     // 사용자별 살아있는 명함 개수
    long countByUserIdxAndIsDeletedFalse(Long userIdx);

    // 사용자별 이름+이메일로 살아있는 명함 존재 여부
    boolean existsByUserIdxAndNameAndEmailAndIsDeletedFalse(Long userIdx, String name, String email);

    // ✅ 소프트 삭제된 명함 조회
    Page<BizCard> findByUserIdxAndIsDeletedTrue(Long userIdx, Pageable pageable);

    // ✅ 여러 id + 살아있는 것만 페이징
    Page<BizCard> findByIdxInAndIsDeletedFalse(List<Long> ids, Pageable pageable);
}
