package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingBookmark;
import soulfit.soulfit.meeting.repository.MeetingBookmarkRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeetingBookmarkService {

    private final MeetingRepository meetingRepository;
    private final MeetingBookmarkRepository meetingBookmarkRepository;

    @Transactional
    public void bookmarkOrUnBookmark(Long meetingId, @AuthenticationPrincipal UserAuth user){
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("모임 없음"));

        Optional<MeetingBookmark> existing = meetingBookmarkRepository.findByMeetingAndUser(meeting, user);

        if (existing.isPresent()){
            meeting.removeBookmark(existing.get());
            meetingBookmarkRepository.delete(existing.get());
        }else{
            MeetingBookmark bookmark = MeetingBookmark.builder().user(user).build();
            meeting.addBookmark(bookmark);
            meetingBookmarkRepository.save(bookmark);

        }

    }

    @Transactional(readOnly = true)
    public Page<Meeting> getBookmarkedMeetingsByUser(UserAuth user, Pageable pageable) {
        return meetingBookmarkRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(MeetingBookmark::getMeeting);
    }
}
