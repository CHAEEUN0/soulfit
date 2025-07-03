package soulfit.soulfit.meeting.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Meeting {

    @Id @GeneratedValue
    @Column(name = "meeting_id")
    private Long id;

    private String name;

    @Lob
    private String description;

    //참가비
    private int price;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private MeetingStatus status;

    //최대인원
    private int capacity;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    @OneToMany(mappedBy = "meeting")
    private List<MeetingMember> members = new ArrayList<>();

    @Embedded
    private Address location;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss:SSS", timezone = "Asia/Seoul")
    private LocalDateTime date;


    public Long getId() {
        return id;
    }
}
