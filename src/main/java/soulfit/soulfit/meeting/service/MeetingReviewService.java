package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.post.Post;
import soulfit.soulfit.community.post.PostImage;
import soulfit.soulfit.community.post.PostService;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingReview;
import soulfit.soulfit.meeting.dto.MeetingReviewRequestDto;
import soulfit.soulfit.meeting.dto.MeetingReviewUpdateRequestDto;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.meeting.repository.MeetingReviewRepository;
import soulfit.soulfit.meeting.domain.HostProFile;
import soulfit.soulfit.meeting.repository.HostProfileRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingReviewService {


    private final MeetingRepository meetingRepository;
    private final MeetingReviewRepository meetingReviewRepository;
    private final PostService postService;
    private final HostProfileRepository hostProfileRepository;


    @Transactional(readOnly = true)
    public Page<MeetingReview> getAllReviews(Long meetingId, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(),  pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return meetingReviewRepository.findByMeetingId(meetingId, sortedPageable);
    }

    @Transactional(readOnly = true)
    public Page<MeetingReview> getUserReviews(UserAuth user, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return meetingReviewRepository.findByUser(user, sortedPageable);
    }


    @Transactional
    public MeetingReview createReview(Long meetingId, MeetingReviewRequestDto requestDto, UserAuth user){
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 모임입니다."));

        MeetingReview meetingReview = requestDto.toEntity(meeting, user);

        //게시글 자동생성
        Post post = postService.createReviewPost(requestDto.getContent(), requestDto.getImages(), user);

        List<String> imageUrls = post.getImages().stream().map(PostImage::getImageUrl).toList();

        meetingReview.setPostImageUrls(imageUrls);
        meetingReview.setPostId(post.getId());

        MeetingReview saved = meetingReviewRepository.save(meetingReview);
        updateHostAverageRating(meeting.getHost().getId());

        return saved;
    }


    @Transactional
    public MeetingReview updateReview(MeetingReviewUpdateRequestDto requestDto, Long reviewId, UserAuth user){


        MeetingReview meetingReview = meetingReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰가 존재하지 않습니다."));

        if (!meetingReview.getUser().getId().equals(user.getId())){
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        meetingReview.updateReview(requestDto.getMeetingRating(), requestDto.getHostRating(), requestDto.getContent());
        meetingReview.setUpdatedAt(LocalDateTime.now());

        Post post = postService.updateReviewPost(requestDto.getContent(), requestDto.getImages(), meetingReview.getPostId(), user);
        meetingReview.setPostImageUrls(post.getImages().stream().map(PostImage::getImageUrl).toList());

        updateHostAverageRating(meetingReview.getMeeting().getHost().getId());

        return meetingReview;
    }

    @Transactional
    public void deleteReview(Long reviewId, UserAuth user){
        MeetingReview meetingReview = meetingReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰가 존재하지 않습니다."));

        if (!meetingReview.getUser().getId().equals(user.getId())){
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        postService.deletePost(meetingReview.getPostId(), user);
        meetingReviewRepository.delete(meetingReview);
        updateHostAverageRating(meetingReview.getMeeting().getHost().getId());
    }
    
    private void updateHostAverageRating(Long hostId){
        HostProFile hostProFile = hostProfileRepository.findById(hostId)
                .orElseThrow(() -> new RuntimeException("호스트 프로필이 존재하지 않습니다."));
        Double hostAvgRating = meetingReviewRepository.findAverageHostRatingByHostId(hostId);
        hostProFile.setHostAverageRating(hostAvgRating!= null ?  Math.round(hostAvgRating * 10.0) / 10.0 : 0.0);

    }
}
