package org.socialculture.platform.performance.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.socialculture.platform.member.entity.QMemberEntity;
import org.socialculture.platform.performance.dto.domain.CategoryContent;
import org.socialculture.platform.performance.dto.domain.PerformanceDetail;
import org.socialculture.platform.performance.dto.domain.PerformanceWithCategory;
import org.socialculture.platform.performance.entity.PerformanceStatus;
import org.socialculture.platform.performance.entity.QCategoryEntity;
import org.socialculture.platform.performance.entity.QPerformanceCategoryEntity;
import org.socialculture.platform.performance.entity.QPerformanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.socialculture.platform.performance.entity.PerformanceStatus.NOT_CONFIRMED;

public class PerformanceRepositoryCustomImpl implements PerformanceRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public PerformanceRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    QPerformanceEntity qPerformanceEntity = QPerformanceEntity.performanceEntity;
    QPerformanceCategoryEntity qPerformanceCategoryEntity = QPerformanceCategoryEntity.performanceCategoryEntity;
    QMemberEntity qMember = QMemberEntity.memberEntity;
    QCategoryEntity qCategoryEntity = QCategoryEntity.categoryEntity;

    public static BooleanBuilder nullSafeBuilder(Supplier<BooleanExpression> f) {
        try {
            return new BooleanBuilder(f.get());
        } catch (IllegalArgumentException e) {
            return new BooleanBuilder();
        }
    }

    private BooleanBuilder performanceStatusNe(PerformanceStatus status) {
        return nullSafeBuilder(() -> qPerformanceEntity.performanceStatus.eq(status));
    }

    private BooleanBuilder performanceIdEq(Long performanceId) {
        return nullSafeBuilder(() -> qPerformanceEntity.performanceId.eq(performanceId));
    }

    private BooleanBuilder performanceCategoryEq(Long performanceId) {
        return nullSafeBuilder(() -> qPerformanceCategoryEntity.performance.performanceId.eq(performanceId));
    }

    private BooleanBuilder memberEmailEq(String memberEmail) {
        return nullSafeBuilder(() -> qMember.email.eq(memberEmail));
    }

    private BooleanBuilder categoryIdEq(Long categoryId) {
        return nullSafeBuilder(() -> qPerformanceCategoryEntity.category.categoryId.eq(categoryId));
    }

    private BooleanBuilder performanceTitleLike(String search) {
        if (search == null || search.trim().isEmpty()) {
            return new BooleanBuilder();
        }
        return new BooleanBuilder(qPerformanceEntity.title.contains(search));
    }

    private BooleanBuilder categoryListWhereClause(PerformanceStatus status, Long categoryId, String search) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(performanceStatusNe(status));
        builder.and(categoryIdEq(categoryId));
        builder.and(performanceTitleLike(search));
        return builder;
    }

    /**
     * 공연 리스트 조회
     * @author Icecoff22
     * @param pageable
     * @return PerformanceWithCategory 형태의 리스트
     *
     */
    @Override
    public Page<PerformanceWithCategory> getPerformanceWithCategoryList(Pageable pageable, Long categoryId, String search) {

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
                .leftJoin(qMember).on(qPerformanceEntity.member.eq(qMember))
                .leftJoin(qPerformanceCategoryEntity).on(qPerformanceCategoryEntity.performance.eq(qPerformanceEntity))
                .where(categoryListWhereClause(NOT_CONFIRMED, categoryId, search))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (performances.isEmpty()) {
            return new PageImpl<>(addCategoriesToPerformances(performances), pageable, 0);
        }

        long totalCount = jpaQueryFactory
                .select(qPerformanceEntity.count())
                .from(qPerformanceEntity)
                .leftJoin(qPerformanceCategoryEntity).on(qPerformanceCategoryEntity.performance.eq(qPerformanceEntity))
                .where(categoryListWhereClause(NOT_CONFIRMED, categoryId, search))
                .fetch().size();

        return new PageImpl<>(addCategoriesToPerformances(performances), pageable, totalCount);
    }

    private List<PerformanceWithCategory> addCategoriesToPerformances(List<PerformanceWithCategory> performances) {
        for (PerformanceWithCategory performance : performances) {
            List<CategoryContent> categories = getCategoriesByPerformance(performance.getPerformanceId());
            performance.updateCategories(categories);
        }
        return performances;
    }

    private List<CategoryContent> getCategoriesByPerformance(Long performanceId) {
        // 조회된 공연 대상으로 각 공연당 카테고리 조회
        return jpaQueryFactory.select(Projections.constructor(CategoryContent.class,
                        qCategoryEntity.categoryId,
                        qCategoryEntity.nameEn,
                        qCategoryEntity.nameKr))
                .from(qCategoryEntity)
                .join(qPerformanceCategoryEntity).on(qPerformanceCategoryEntity.category.eq(qCategoryEntity))
                .where(performanceCategoryEq(performanceId))
                .fetch();
    }

    /**
     * 공연 상세 조회
     * @author Icecoff22
     * @param performanceId
     * @return Optional PerformanceDetail dto
     *
     */
    @Override
    public Optional<PerformanceDetail> getPerformanceDetail(Long performanceId) {
        PerformanceDetail performanceDetail = jpaQueryFactory.select(Projections.constructor(PerformanceDetail.class,
                        qMember.name.as("memberName"),
                        qPerformanceEntity.performanceId.as("performanceId"),
                        qPerformanceEntity.title.as("title"),
                        qPerformanceEntity.dateStartTime.as("dateStartTime"),
                        qPerformanceEntity.dateEndTime.as("dateEndTime"),
                        qPerformanceEntity.description.as("description"),
                        qPerformanceEntity.maxAudience.as("maxAudience"),
                        qPerformanceEntity.address.as("address"),
                        qPerformanceEntity.imageUrl.as("imageUrl"),
                        qPerformanceEntity.price.as("price"),
                        qPerformanceEntity.remainingTickets.as("remainingTickets"),
                        qPerformanceEntity.startDate.as("startDate"),
                        qPerformanceEntity.performanceStatus.as("status"),
                        qPerformanceEntity.createdAt.as("createdAt"),
                        qPerformanceEntity.updatedAt.as("updatedAt")
                ))
                .from(qPerformanceEntity)
                .leftJoin(qMember).on(qPerformanceEntity.member.eq(qMember))
                .where(performanceIdEq(performanceId))
                .fetchOne();

        if (performanceDetail != null) {
            List<CategoryContent> categories = getCategoriesByPerformance(performanceDetail.getPerformanceId());
            performanceDetail.updateCategories(categories);
        }

        return Optional.ofNullable(performanceDetail);
    }

    @Override
    public Page<PerformanceWithCategory> getMyPerformanceWithCategoryList(String email, Pageable pageable) {
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
                .leftJoin(qMember).on(qPerformanceEntity.member.eq(qMember))
                .where(memberEmailEq(email))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (performances.isEmpty()) {
            return new PageImpl<>(addCategoriesToPerformances(performances), pageable, 0);
        }

        long totalCount = jpaQueryFactory
                .select(qPerformanceEntity.count())
                .from(qPerformanceEntity)
                .leftJoin(qMember).on(qPerformanceEntity.member.eq(qMember))
                .where(memberEmailEq(email))
                .fetch().size();

        return new PageImpl<>(addCategoriesToPerformances(performances), pageable, totalCount);
    }
}
