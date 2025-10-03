package soulfit.soulfit.matching.voting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.voting.domain.UserVote;
import soulfit.soulfit.matching.voting.domain.VoteForm;
import soulfit.soulfit.matching.voting.domain.VoteOption;
import soulfit.soulfit.matching.voting.dto.*;
import soulfit.soulfit.matching.voting.repository.UserVoteRepository;
import soulfit.soulfit.matching.voting.repository.VoteFormRepository;
import soulfit.soulfit.matching.voting.repository.VoteOptionRepository;
import soulfit.soulfit.profile.repository.UserProfileRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteFormRepository voteFormRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final UserVoteRepository userVoteRepository;
    private final UserRepository userRepository; // Assume exists
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public Page<VoteFormResponse> getAllVoteForms(Pageable pageable) {
        Page<VoteForm> voteForms = voteFormRepository.findAllByActiveTrueOrderByCreatedAtDesc(pageable);
        return voteForms.map(this::convertToVoteFormResponse); // 엔티티를 DTO로 변환
    }

    private VoteFormResponse convertToVoteFormResponse(VoteForm voteForm) {
        List<VoteOptionResponse> optionResponses = voteOptionRepository
                .findByVoteFormIdOrderBySortOrderAsc(voteForm.getId())
                .stream()
                .map(option -> new VoteOptionResponse(option.getId(), option.getLabel(), option.getEmoji()))
                .toList();

        String creatorProfileImageUrl = userProfileRepository.findByUserAuthId(voteForm.getCreator().getId())
                .map(userProfile -> userProfile.getProfileImageUrl())
                .orElse(null);

        return new VoteFormResponse(
                voteForm.getId(),
                voteForm.getTitle(),
                voteForm.getCreator().getId(),
                voteForm.getCreator().getUsername(),
                creatorProfileImageUrl,
                voteForm.getTargetType().name(),
                voteForm.getImageUrl(),
                optionResponses
        );
    }

    @Transactional(readOnly = true)
    public VoteFormResponse getVoteForm(Long voteFormId) {
        VoteForm voteForm = voteFormRepository.findByIdAndActiveTrue(voteFormId)
                .orElseThrow(() -> new IllegalArgumentException("투표 폼을 찾을 수 없습니다."));
        return convertToVoteFormResponse(voteForm);
    }

    @Transactional
    public void vote(Long voterId, VoteRequest request) {
        VoteForm voteForm = voteFormRepository.findByIdAndActiveTrue(request.getVoteFormId())
                .orElseThrow(() -> new IllegalArgumentException("투표 폼 없음"));

        UserAuth targetUser = voteForm.getCreator();

        if (userVoteRepository.existsByVoterIdAndTargetUserIdAndVoteFormId(voterId, targetUser.getId(), request.getVoteFormId())) {
            throw new IllegalStateException("이미 투표했습니다.");
        }

        UserAuth voter = userRepository.findById(voterId)
                .orElseThrow(() -> new IllegalArgumentException("투표자 없음"));
        VoteOption voteOption = voteOptionRepository.findById(request.getVoteOptionId())
                .orElseThrow(() -> new IllegalArgumentException("선택지 없음"));

        UserVote vote = UserVote.createVote(voter, targetUser, voteForm, voteOption);

        userVoteRepository.save(vote);
    }

    @Transactional
    public Long createVoteForm(UserAuth creator, VoteFormCreateRequest request) {
        VoteForm.TargetType targetType = VoteForm.TargetType.valueOf(request.getTargetType().toUpperCase());

        if (targetType == VoteForm.TargetType.IMAGE && (request.getImageUrl() == null || request.getImageUrl().isEmpty())) {
            throw new IllegalArgumentException("이미지 투표 양식에는 imageUrl이 필수입니다.");
        }

        VoteForm voteForm = VoteForm.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .creator(creator)
                .active(true)
                .multiSelect(false) // 기본값 설정, 필요시 요청에서 받을 수 있음
                .targetType(targetType)
                .imageUrl(request.getImageUrl()) // imageUrl 설정
                .build();

        List<VoteOption> options = request.getOptions().stream()
                .map(optionLabel -> {
                    int index = request.getOptions().indexOf(optionLabel);
                    return VoteOption.builder()
                            .label(optionLabel)
                            .voteForm(voteForm)
                            .sortOrder(index)
                            .build();
                })
                .collect(Collectors.toList());

        voteForm.getOptions().addAll(options);

        voteFormRepository.save(voteForm);
        return voteForm.getId();
    }

    @Transactional(readOnly = true)
    public VoteResultResponse getVoteResults(UserAuth user, Long voteFormId) {
        VoteForm voteForm = voteFormRepository.findById(voteFormId)
                .orElseThrow(() -> new IllegalArgumentException("투표 폼을 찾을 수 없습니다."));

        if (!voteForm.getCreator().getId().equals(user.getId())) {
            throw new IllegalStateException("투표 결과를 조회할 권한이 없습니다.");
        }

        List<VoteResultResponse.OptionResult> results = userVoteRepository.getVoteResults(voteFormId);
        return new VoteResultResponse(voteFormId, voteForm.getTitle(), results);
    }
}