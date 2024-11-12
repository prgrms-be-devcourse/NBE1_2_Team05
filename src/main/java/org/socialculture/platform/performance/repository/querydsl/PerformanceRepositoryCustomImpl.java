package org.socialculture.platform.performance.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.locationtech.jts.geom.Point;
import org.socialculture.platform.global.apiResponse.exception.ErrorStatus;
import org.socialculture.platform.global.apiResponse.exception.GeneralException;
import org.socialculture.platform.member.entity.QMemberCategoryEntity;
import org.socialculture.platform.member.entity.QMemberEntity;
import org.socialculture.platform.performance.dto.domain.CategoryContent;
import org.socialculture.platform.performance.dto.domain.PerformanceDetail;
import org.socialculture.platform.performance.dto.domain.PerformanceWithCategory;
import org.socialculture.platform.performance.entity.PerformanceStatus;
import org.socialculture.platform.performance.entity.QCategoryEntity;
import org.socialculture.platform.performance.entity.QPerformanceCategoryEntity;
import org.socialculture.platform.performance.entity.QPerformanceEntity;
import org.socialculture.platform.ticket.entity.QTicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.function.Supplier;

import static org.socialculture.platform.performance.entity.PerformanceStatus.CONFIRMED;
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
    QMemberCategoryEntity qMemberCategoryEntity = QMemberCategoryEntity.memberCategoryEntity;

    public static BooleanBuilder nullSafeBuilder(Supplier<BooleanExpression> f) {
        try {
            return new BooleanBuilder(f.get());
        } catch (IllegalArgumentException e) {
            return new BooleanBuilder();
        }
    }

    private BooleanBuilder performanceStatusEq(PerformanceStatus status) {
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

    private BooleanBuilder categoryListWhereClause(Long categoryId, String search, String email) {
        BooleanBuilder builder = new BooleanBuilder();

        // 첫 번째 조건: 카테고리 ID와 공연 제목이 같고 상태가 CONFIRMED
        BooleanBuilder confirmedCondition = new BooleanBuilder();
        confirmedCondition.and(categoryIdEq(categoryId))
                .and(performanceTitleLike(search))
                .and(performanceStatusEq(CONFIRMED));

        // 두 번째 조건: 카테고리 ID와 공연 제목이 같고 이메일이 같고 상태가 NOT_CONFIRMED
        BooleanBuilder notConfirmedCondition = new BooleanBuilder();
        notConfirmedCondition.and(categoryIdEq(categoryId))
                .and(performanceTitleLike(search))
                .and(memberEmailEq(email))
                .and(performanceStatusEq(NOT_CONFIRMED));

        // 두 조건을 or로 결합
        builder.or(confirmedCondition).or(notConfirmedCondition);

        return builder;
    }

    private BooleanTemplate getContainsBooleanExpression(Point location, Integer radius) {
        String geoFunction = "ST_CONTAINS(ST_BUFFER(ST_GeomFromText('%s', 4326), {0}), coordinate)";
        String expression = String.format(geoFunction, location);

        return Expressions.booleanTemplate(expression, radius);
    }

    private OrderSpecifier<?> getOrderSpecifiersByDistance(Point location) {
        String geoFunction = "ST_Distance_Sphere(coordinate, ST_GeomFromText('%s', 4326))";
        String formattedGeoFunction = String.format(geoFunction, location);

        return new OrderSpecifier<>(Order.ASC, Expressions.numberTemplate(Double.class, formattedGeoFunction));
    }



    /**
     * 공연 리스트 조회
     *
     * @param pageable
     * @return PerformanceWithCategory 형태의 리스트
     * @author Icecoff22
     */
    @Override
    public Page<PerformanceWithCategory> getPerformanceWithCategoryList(Pageable pageable, Long categoryId, String search, String email) {

        List<PerformanceWithCategory> performances = jpaQueryFactory.selectDistinct(Projections.constructor(PerformanceWithCategory.class,
                        qMember.name.as("memberName"),
                        qPerformanceEntity.performanceId.as("performanceId"),
                        qPerformanceEntity.title.as("title"),
                        qPerformanceEntity.dateStartTime.as("dateStartTime"),
                        qPerformanceEntity.dateEndTime.as("dateEndTime"),
                        qPerformanceEntity.address.as("address"),
                        qPerformanceEntity.imageUrl.as("imageUrl"),
                        qPerformanceEntity.price.as("price"),
                        qPerformanceEntity.performanceStatus.as("status"),
                        qPerformanceEntity.remainingTickets.as("remainingTicket")
                ))
                .from(qPerformanceEntity)
                .leftJoin(qMember).on(qPerformanceEntity.member.eq(qMember))
                .leftJoin(qPerformanceCategoryEntity).on(qPerformanceCategoryEntity.performance.eq(qPerformanceEntity))
                .where(categoryListWhereClause(categoryId, search, email))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (performances.isEmpty()) {
            return new PageImpl<>(addCategoriesToPerformances(performances), pageable, 0);
        }

        Long totalCount = jpaQueryFactory
                .select(qPerformanceEntity.count())
                .from(qPerformanceEntity)
                .leftJoin(qMember).on(qPerformanceEntity.member.eq(qMember))
                .leftJoin(qPerformanceCategoryEntity).on(qPerformanceCategoryEntity.performance.eq(qPerformanceEntity))
                .where(categoryListWhereClause(categoryId, search, email))
                .fetchOne();

        if (totalCount == null) {
            totalCount = 0L;
        }

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
     * 사용자가 선호하는 카테고리를 기반으로 추천 공연 조회
     * 매칭되는 카테고리 개수 순으로 최대 10개의 공연 내림차순 정렬
     *
     * @param memberId
     * @return
     */
    @Override
    public List<PerformanceWithCategory> getRecommendedPerformancesByMember(Long memberId) {

        // 사용자 선호 카테고리와 매칭된 공연 조회
        List<Tuple> results = jpaQueryFactory
                .select(qMember.name,
                        qPerformanceEntity.performanceId,
                        qPerformanceEntity.title,
                        qPerformanceEntity.dateStartTime,
                        qPerformanceEntity.dateEndTime,
                        qPerformanceEntity.address,
                        qPerformanceEntity.imageUrl,
                        qPerformanceEntity.price,
                        qPerformanceEntity.performanceStatus,
                        qPerformanceEntity.remainingTickets,
                        qCategoryEntity.categoryId,
                        qCategoryEntity.nameKr,
                        qCategoryEntity.nameEn)
                .from(qPerformanceCategoryEntity)
                .join(qPerformanceEntity).on(qPerformanceCategoryEntity.performance.eq(qPerformanceEntity))
                .join(qMember).on(qPerformanceEntity.member.eq(qMember))
                .join(qCategoryEntity).on(qPerformanceCategoryEntity.category.eq(qCategoryEntity))
                .join(qMemberCategoryEntity).on(qPerformanceCategoryEntity.category.eq(qMemberCategoryEntity.category))
                .where(qMemberCategoryEntity.member.memberId.eq(memberId)
                        .and(qPerformanceEntity.performanceStatus.eq(CONFIRMED)))
                .groupBy(qPerformanceEntity.performanceId,
                        qCategoryEntity.categoryId)
                .limit(10)
                .fetch();

        // PerformanceWithCategory 객체로 매핑
        Map<Long, PerformanceWithCategory> performanceMap = new HashMap<>();

        results.forEach(tuple -> {
            Long performanceId = tuple.get(qPerformanceEntity.performanceId);

            // performanceId 기준으로 이미 존재하는 PerformanceWithCategory 객체가 있는지 확인
            PerformanceWithCategory performance = performanceMap.computeIfAbsent(
                    performanceId, k -> new PerformanceWithCategory(
                            tuple.get(qMember.name),
                            performanceId,
                            tuple.get(qPerformanceEntity.title),
                            tuple.get(qPerformanceEntity.dateStartTime),
                            tuple.get(qPerformanceEntity.dateEndTime),
                            tuple.get(qPerformanceEntity.address),
                            tuple.get(qPerformanceEntity.imageUrl),
                            tuple.get(qPerformanceEntity.price),
                            tuple.get(qPerformanceEntity.performanceStatus),
                            tuple.get(qPerformanceEntity.remainingTickets)
                    ));

            // 카테고리 리스트에 카테고리 추가
            List<CategoryContent> categories
                    = performance.getCategories() != null ? performance.getCategories() : new ArrayList<>();
            categories.add(new CategoryContent(
                    tuple.get(qCategoryEntity.categoryId),
                    tuple.get(qCategoryEntity.nameKr),
                    tuple.get(qCategoryEntity.nameEn)
            ));
            performance.updateCategories(categories);
        });

        // 카테고리 개수로 내림차순 정렬
        List<PerformanceWithCategory> sortedPerformances = new ArrayList<>(performanceMap.values());
        sortedPerformances.sort((p1, p2) -> Integer.compare(p2.getCategories().size(), p1.getCategories().size()));

        return sortedPerformances;
    }

    /**
     * 실시간 인기공연을 조회한다.
     *
     * @param performanceIds
     * @return
     */
    @Override
    public List<PerformanceWithCategory> getPerformancesByIds(List<Long> performanceIds) {

        List<PerformanceWithCategory> performances = jpaQueryFactory
                .selectDistinct(Projections.constructor(PerformanceWithCategory.class,
                        qMember.name.as("memberName"),
                        qPerformanceEntity.performanceId.as("performanceId"),
                        qPerformanceEntity.title.as("title"),
                        qPerformanceEntity.dateStartTime.as("dateStartTime"),
                        qPerformanceEntity.dateEndTime.as("dateEndTime"),
                        qPerformanceEntity.address.as("address"),
                        qPerformanceEntity.imageUrl.as("imageUrl"),
                        qPerformanceEntity.price.as("price"),
                        qPerformanceEntity.performanceStatus.as("status"),
                        qPerformanceEntity.remainingTickets.as("remainingTicket")
                ))
                .from(qPerformanceEntity)
                .leftJoin(qMember).on(qPerformanceEntity.member.eq(qMember))
                .leftJoin(qPerformanceCategoryEntity).on(qPerformanceCategoryEntity.performance.eq(qPerformanceEntity))
                .where(qPerformanceEntity.performanceId.in(performanceIds))
                .fetch();

        return addCategoriesToPerformances(performances);
    }


    /**
     * 공연 상세 조회
     *
     * @param performanceId
     * @return Optional PerformanceDetail dto
     * @author Icecoff22
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
                        qPerformanceEntity.location.as("location"),
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
                        qPerformanceEntity.performanceStatus.as("status"),
                        qPerformanceEntity.remainingTickets.as("remainingTicket")
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

        Long totalCount = jpaQueryFactory
                .select(qPerformanceEntity.count())
                .from(qPerformanceEntity)
                .leftJoin(qMember).on(qPerformanceEntity.member.eq(qMember))
                .where(memberEmailEq(email))
                .fetchOne();

        if (totalCount == null) {
            totalCount = 0L;
        }

        return new PageImpl<>(addCategoriesToPerformances(performances), pageable, totalCount);
    }

    /**
     * 특정 좌표에서 가까운 순서대로 공연을 조회한다.
     * @Author Icecoff22
     * @param location : geo 좌표
     * @param radius : 좌표 중심 원(~km까지)
     * @param pageable : 페이징
     * @return
     */
    @Override
    public Page<PerformanceWithCategory> getPerformanceAroundPoint(Point location, Integer radius, Pageable pageable) {
        // 사용자 선호 카테고리와 매칭된 공연 조회
        List<PerformanceWithCategory> performances = jpaQueryFactory.select(Projections.constructor(PerformanceWithCategory.class,
                        qMember.name.as("memberName"),
                        qPerformanceEntity.performanceId.as("performanceId"),
                        qPerformanceEntity.title.as("title"),
                        qPerformanceEntity.dateStartTime.as("dateStartTime"),
                        qPerformanceEntity.dateEndTime.as("dateEndTime"),
                        qPerformanceEntity.address.as("address"),
                        qPerformanceEntity.imageUrl.as("imageUrl"),
                        qPerformanceEntity.price.as("price"),
                        qPerformanceEntity.performanceStatus.as("status"),
                        qPerformanceEntity.remainingTickets.as("remainingTicket")
                ))
                .from(qPerformanceEntity)
                .leftJoin(qMember).on(qPerformanceEntity.member.eq(qMember))
                .where(getContainsBooleanExpression(location, radius))
                .orderBy(getOrderSpecifiersByDistance(location))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (performances.isEmpty()) {
            return new PageImpl<>(addCategoriesToPerformances(performances), pageable, 0);
        }

        Long totalCount = jpaQueryFactory
                .select(qPerformanceEntity.count())
                .from(qPerformanceEntity)
                .leftJoin(qMember).on(qPerformanceEntity.member.eq(qMember))
                .where(getContainsBooleanExpression(location, radius))
                .fetchOne();

        if (totalCount == null) {
            totalCount = 0L;
        }

        return new PageImpl<>(addCategoriesToPerformances(performances), pageable, totalCount);
    }
}
