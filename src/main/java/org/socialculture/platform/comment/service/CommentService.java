package org.socialculture.platform.comment.service;


import org.socialculture.platform.comment.dto.request.CommentCreateRequest;
import org.socialculture.platform.comment.dto.request.CommentUpdateRequest;
import org.socialculture.platform.comment.dto.response.CommentDeleteResponse;
import org.socialculture.platform.comment.dto.response.CommentReadDto;
import org.socialculture.platform.comment.dto.response.CommentUpdateResponse;

import java.util.List;

public interface CommentService {
    List<CommentReadDto> getAllComment(long performanceId);// 댓글 전체조회

    boolean createComment(long performanceId, CommentCreateRequest commentCreateRequest);

    CommentUpdateResponse updateComment(long commentId, CommentUpdateRequest commentUpdateRequest);

    CommentDeleteResponse deleteComment(long commentId);
}
