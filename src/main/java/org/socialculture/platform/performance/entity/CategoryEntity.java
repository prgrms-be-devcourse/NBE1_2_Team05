package org.socialculture.platform.performance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.socialculture.platform.member.entity.MemberCategoryEntity;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "name_kr", nullable = false)
    private String nameKr;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerformanceCategoryEntity> performanceCategoryList = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberCategoryEntity> memberCategories = new ArrayList<>();


    // 테스트에서 사용
    @Builder
    public CategoryEntity(Long categoryId, String nameKr, String nameEn) {
        this.categoryId = categoryId;
        this.nameKr = nameKr;
        this.nameEn = nameEn;
    }
}
