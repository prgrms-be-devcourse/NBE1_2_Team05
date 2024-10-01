package org.socialculture.platform.comment.service;

import org.socialculture.platform.comment.dto.request.CommentCreateRequest;
import org.socialculture.platform.comment.dto.request.CommentUpdateRequest;
import org.socialculture.platform.comment.dto.response.CommentDeleteResponse;
import org.socialculture.platform.comment.dto.response.CommentReadDto;
import org.socialculture.platform.comment.dto.response.CommentUpdateResponse;
import org.socialculture.platform.comment.entity.CommentEntity;
import org.socialculture.platform.comment.entity.CommentStatus;
import org.socialculture.platform.comment.repository.CommentRepository;
import org.socialculture.platform.global.apiResponse.exception.ErrorStatus;
import org.socialculture.platform.global.apiResponse.exception.GeneralException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
//    private PerformanceRepository performanceRepository;// performanceRepo도 필요하겠지?

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
//        this.performanceRepository=performanceRepository;
    }

    @Override
    public List<CommentReadDto> getAllComment(long performanceId) {


        // 주어진 id값으로 댓글 테이블 조회하믄댐
        List<CommentEntity> commentEntityList = commentRepository.findAllByPerformance_PerformanceId(performanceId);

        if (commentEntityList == null) {
            throw new GeneralException(ErrorStatus.PERFORMANCE_NOT_FOUND);// 예외처리 하기 전
        }
        List<CommentReadDto> commentReadDtos = new ArrayList<>();

        for (CommentEntity commentEntity : commentEntityList) {
            CommentReadDto commentReadDto = CommentReadDto.builder()
                    .commentId(commentEntity.getCommentId())
                    .memberId(1)//아직은 테스트
                    .content(commentEntity.getContent())
                    .createdAt(commentEntity.getCreatedAt())
                    .updatedAt(commentEntity.getUpdatedAt())
                    .parentId(commentEntity.getParentId())
                    .commentStatus(commentEntity.getCommentStatus())
                    .build();
            commentReadDtos.add(commentReadDto);
        }
        return commentReadDtos;
    }

    /*
     * 댓글 생성*/
    @Override
    public boolean createComment(long performanceId, CommentCreateRequest commentCreateRequest) {

//        PerformanceEntity performance = performanceRepository.findById(performanceId)
//                .orElseThrow(() -> new GeneralException(ErrorStatus.PERFORMANCE_NOT_FOUND));
        CommentEntity commentEntity = CommentEntity.builder()
                .content(commentCreateRequest.content())
                .parentId(commentCreateRequest.parentId())
                .commentStatus(CommentStatus.ACTIVE)
//                .performance(performance)
                .build();
        System.out.println(commentCreateRequest.content() + "입니다");
        commentRepository.save(commentEntity);
        return true;
    }//일단 userid도 없어서 보류

    @Override
    public CommentUpdateResponse updateComment(long commentId, CommentUpdateRequest commentUpdateRequest) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));//comment 객체찾아주고

        commentEntity.builder()
                .content(commentUpdateRequest.content())
                .updatedAt(LocalDateTime.now())
                .build();

        commentRepository.save(commentEntity);

        CommentUpdateResponse commentUpdateResponse = CommentUpdateResponse.from(commentEntity.getPerformance().getPerformanceId());

        return commentUpdateResponse;
    }

    @Override
    public CommentDeleteResponse deleteComment(long commentId) {
        // 댓글이 존재하지 않을 경우 예외를 던지고, 존재할 경우 바로 삭제
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));

        CommentDeleteResponse commentDeleteResponse = CommentDeleteResponse.of(commentEntity.getPerformance().getPerformanceId());

        commentRepository.delete(commentEntity);

        return  commentDeleteResponse;
    }

}
