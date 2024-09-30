package org.socialculture.platform.comment.repository;

import org.socialculture.platform.comment.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity,Long> {
    List<CommentEntity> findAllByPerformanceId(long performanceId);
}
