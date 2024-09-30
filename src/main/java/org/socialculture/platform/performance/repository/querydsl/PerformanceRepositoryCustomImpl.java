package org.socialculture.platform.performance.repository.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.socialculture.platform.member.entity.QMember;
import org.socialculture.platform.performance.dto.CategoryDTO;
import org.socialculture.platform.performance.dto.PerformanceWithCategory;
import org.socialculture.platform.performance.entity.QCategoryEntity;
import org.socialculture.platform.performance.entity.QPerformanceCategoryEntity;
import org.socialculture.platform.performance.entity.QPerformanceEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.socialculture.platform.performance.entity.PerformanceStatus.NOT_CONFIRMED;

public class PerformanceRepositoryCustomImpl implements PerformanceRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public PerformanceRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    QPerformanceEntity qPerformanceEntity = QPerformanceEntity.performanceEntity;
    QPerformanceCategoryEntity qPerformanceCategoryEntity = QPerformanceCategoryEntity.performanceCategoryEntity;
    QMember qMember = QMember.member;
    QCategoryEntity qCategoryEntity = QCategoryEntity.categoryEntity;

    /**
     * 공연 리스트 조회
     * @author 정승주
     * @param pageable
     * @return PerformanceWithCategory 형태의 리스트
     *
     */
    @Override
    public List<PerformanceWithCategory> getPerformanceWithCategoryList(Pageable pageable) {

        List<PerformanceWithCategory> performances = jpaQueryFactory.select(Projections.constructor(PerformanceWithCategory.class,
                        qMember.name.as("memberName"),
                        qPerformanceEntity.performanceId.as("performanceId"),
                        qPerformanceEntity.title.as("title"),
                        qPerformanceEntity.dateStartTime.as("dateStartTime"),
                        qPerformanceEntity.dateEndTime.as("dateEndTime"),
                        qPerformanceEntity.address.as("address"),
                        qPerformanceEntity.imageUrl.as("imageUrl"),
                        qPerformanceEntity.price.as("price"),
                        qPerformanceEntity.performanceStatus.as("status")
                ))
                .from(qPerformanceEntity)
                .leftJoin(qMember)
                .where(qPerformanceEntity.performanceStatus.ne(NOT_CONFIRMED))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        for (PerformanceWithCategory performance : performances) {
            List<CategoryDTO> categories = jpaQueryFactory.select(Projections.constructor(CategoryDTO.class,
                            qCategoryEntity.categoryId,
                            qCategoryEntity.nameEn,
                            qCategoryEntity.nameKr))
                    .from(qCategoryEntity)
                    .join(qPerformanceCategoryEntity).on(qPerformanceCategoryEntity.category.eq(qCategoryEntity))
                    .where(qPerformanceCategoryEntity.performance.performanceId.eq(performance.getPerformanceId())) // 공연 ID로 필터링
                    .fetch();


            // 카테고리 리스트를 PerformanceWithCategory에 설정
            performance.updateCategories(categories);
        }

        return performances;
    }
}
