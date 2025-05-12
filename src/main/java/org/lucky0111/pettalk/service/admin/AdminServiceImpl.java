package org.lucky0111.pettalk.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.common.UserRole;
import org.lucky0111.pettalk.domain.dto.admin.*;
import org.lucky0111.pettalk.domain.dto.review.ReviewResponseDTO;
import org.lucky0111.pettalk.domain.entity.community.Comment;
import org.lucky0111.pettalk.domain.entity.community.Post;
import org.lucky0111.pettalk.domain.entity.trainer.Certification;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.repository.community.CommentRepository;
import org.lucky0111.pettalk.repository.community.PostRepository;
import org.lucky0111.pettalk.repository.trainer.CertificationRepository;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.service.community.CommentService;
import org.lucky0111.pettalk.service.community.PostService;
import org.lucky0111.pettalk.service.review.ReviewService;
import org.lucky0111.pettalk.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final CertificationRepository certificationRepository;
//    private final TrainerService trainerService;

    private final PetUserRepository petUserRepository;
    private final UserService userService;

//    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    private final CommentRepository commentRepository;
    private final CommentService commentService;

    private final PostRepository postRepository;
    private final PostService postService;


    @Override
    public List<AdminCertificationDTO> getAllCertificationsForAdmin() {

        List<Certification> certifications = certificationRepository.findAllWithTrainerAndUser();

        List<AdminCertificationDTO> certificationDTOS = certifications.stream()
                .map(certification -> new AdminCertificationDTO(
                        certification.getCertId(),
                        certification.getCertName(),
                        certification.getIssuingBody(),
                        certification.getIssueDate(),
                        certification.getFileUrl(),
                        certification.getApproved(),
                        certification.getRejected(),
                        certification.getTrainer().getTrainerId(),
                        certification.getTrainer().getUser().getName(),
                        certification.getTrainer().getUser().getNickname()))
                .toList();

        return certificationDTOS;
    }

    @Override
    public void approveCertificationForAdmin(Long certId) {

        Certification certification = certificationRepository.findById(certId)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found with id: " + certId));

        certification.setRejected(false);
        certification.setApproved(true);



        certificationRepository.save(certification);
    }

    @Override
    public void rejectCertificationForAdmin(Long certId) {

        Certification certification = certificationRepository.findById(certId)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found with id: " + certId));

        certification.setRejected(true);
        certification.setApproved(false);
        certificationRepository.save(certification);
    }


    @Override
    public List<AdminUserDTO> getAllUsersForAdmin() {

        List<PetUser> users = petUserRepository.findAll();

        List<AdminUserDTO> userDTOS = users.stream()
                .map(user -> new AdminUserDTO(
                        user.getUserId(),
                        user.getName(),
                        user.getNickname(),
                        user.getEmail(),
                        user.getProfileImageUrl(),
                        user.getRole(),
                        user.getProvider(),
                        user.getSocialId(),
                        user.getStatus()))
                .toList();

        return userDTOS;
    }

    @Override
    public void updateUserRoleAndStatusForAdmin(UUID userId, UserRole role, String status) {

        PetUser user = petUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (role != null) {
            user.setRole(role);
        }
        if (status != null) {
            user.setStatus(status);
        }

        petUserRepository.save(user);
    }

    @Override
    public void withdrawUserForAdmin(UUID userId) {

        boolean successfulWithdrawal = userService.withdrawUser(userId);

        if (!successfulWithdrawal) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
    }


    @Override
    public List<ReviewResponseDTO> getAllReviewsForAdmin() {

        List<ReviewResponseDTO> reviewDTOS = reviewService.getAllReviews();

        return reviewDTOS;
    }

    @Override
    public void deleteReviewForAdmin(Long reviewId) {

        reviewService.deleteReview(reviewId);
    }


    @Override
    public List<AdminPostDTO> getAllPostsForAdmin() {

        List<Post> posts = postRepository.findAllWithUser();

        List<AdminPostDTO> postDTOS = posts.stream()
                .map(post -> new AdminPostDTO(
                        post.getPostId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getUser().getUserId(),
                        post.getUser().getName(),
                        post.getUser().getNickname()))
                .toList();

        return postDTOS;
    }

    @Override
    public void deletePostForAdmin(Long postId) {

        postService.deletePost(postId);
    }


    @Override
    public List<AdminCommentDTO> getAllCommentsForAdmin() {

        List<Comment> comments = commentRepository.findAllWithUser();

        List<AdminCommentDTO> commentDTOS = comments.stream()
                .map(comment -> new AdminCommentDTO(
                        comment.getCommentId(),
                        comment.getContent(),
                        comment.getUser().getUserId(),
                        comment.getUser().getName(),
                        comment.getUser().getNickname()))
                .toList();

        return commentDTOS;
    }

    @Override
    public void deleteCommentForAdmin(Long commentId) {

        commentService.deleteComment(commentId);
    }

}
