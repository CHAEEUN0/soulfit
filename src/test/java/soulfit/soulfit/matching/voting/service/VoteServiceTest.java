package soulfit.soulfit.matching.voting.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.voting.domain.VoteForm;
import soulfit.soulfit.matching.voting.domain.VoteOption;
import soulfit.soulfit.matching.voting.dto.VoteFormResponse;
import soulfit.soulfit.matching.voting.repository.VoteFormRepository;
import soulfit.soulfit.matching.voting.repository.VoteOptionRepository;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.profile.repository.UserProfileRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @InjectMocks
    private VoteService voteService;

    @Mock
    private VoteFormRepository voteFormRepository;

    @Mock
    private VoteOptionRepository voteOptionRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("전체 투표 목록 조회 - 활성화된 투표가 있을 때")
    void getAllVoteForms_shouldReturnPagedVoteForms_whenActiveFormsExist() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        UserAuth creator = new UserAuth();
        creator.setId(1L);
        creator.setUsername("creator");
        VoteForm voteForm = VoteForm.builder()
                .id(1L)
                .title("Test Vote")
                .creator(creator)
                .targetType(VoteForm.TargetType.PROFILE)
                .build();

        Page<VoteForm> voteFormPage = new PageImpl<>(Collections.singletonList(voteForm), pageable, 1);

        when(voteFormRepository.findAllByActiveTrueOrderByCreatedAtDesc(pageable)).thenReturn(voteFormPage);
        when(voteOptionRepository.findByVoteFormIdOrderBySortOrderAsc(anyLong())).thenReturn(Collections.singletonList(new VoteOption()));
        when(userProfileRepository.findByUserAuthId(anyLong())).thenReturn(Optional.of(new UserProfile()));

        // when
        Page<VoteFormResponse> result = voteService.getAllVoteForms(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(voteForm.getId());
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(voteForm.getTitle());
    }

    @Test
    @DisplayName("전체 투표 목록 조회 - 활성화된 투표가 없을 때")
    void getAllVoteForms_shouldReturnEmptyPage_whenNoActiveFormsExist() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<VoteForm> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(voteFormRepository.findAllByActiveTrueOrderByCreatedAtDesc(pageable)).thenReturn(emptyPage);

        // when
        Page<VoteFormResponse> result = voteService.getAllVoteForms(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }
}
