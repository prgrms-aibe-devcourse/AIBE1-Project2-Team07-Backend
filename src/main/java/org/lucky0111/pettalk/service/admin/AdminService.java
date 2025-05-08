package org.lucky0111.pettalk.service.admin;

import org.lucky0111.pettalk.domain.common.UserRole;
import org.lucky0111.pettalk.domain.dto.admin.*;
import org.lucky0111.pettalk.domain.dto.review.ReviewResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    List<AdminCertificationDTO> getAllCertificationsForAdmin();

    void approveCertificationForAdmin(Long certId);

    void rejectCertificationForAdmin(Long certId);


    List<AdminUserDTO> getAllUsersForAdmin();

    void updateUserRoleAndStatusForAdmin(UUID userId, UserRole role, String status);

    void withdrawUserForAdmin(UUID userId);


    List<ReviewResponseDTO> getAllReviewsForAdmin();

    void deleteReviewForAdmin(Long reviewId);


    List<AdminPostDTO> getAllPostsForAdmin();

    void deletePostForAdmin(Long postId);


    List<AdminCommentDTO> getAllCommentsForAdmin();

    void deleteCommentForAdmin(Long commentId);
}
