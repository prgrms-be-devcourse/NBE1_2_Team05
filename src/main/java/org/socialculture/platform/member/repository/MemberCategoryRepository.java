package org.socialculture.platform.member.repository;

import org.socialculture.platform.member.entity.MemberCategoryEntity;
import org.socialculture.platform.performance.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberCategoryRepository extends JpaRepository<MemberCategoryEntity, Long> {

    @Query("SELECT mc.category FROM MemberCategoryEntity mc WHERE mc.member.memberId = :memberId")
    List<CategoryEntity> findCategoriesByMemberId(Long memberId);

    void deleteByMemberMemberId(Long memberId);
}
