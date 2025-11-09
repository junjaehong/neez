package com.bbey.neez.repository;

import com.bbey.neez.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HashTagRepository extends JpaRepository<HashTag, Long> {
    Optional<HashTag> findByName(String name);

    // ✅ 5번: 많이 쓰는 태그 Top N
    @Query(
        value = "SELECT h.* FROM hashTags h " +
                "JOIN cardHashTags c ON h.idx = c.tag_idx " +
                "GROUP BY h.idx, h.name, h.created_at, h.updated_at " +
                "ORDER BY COUNT(c.idx) DESC " +
                "LIMIT ?1",
        nativeQuery = true
    )
    List<HashTag> findTopUsedTags(int limit);
}
