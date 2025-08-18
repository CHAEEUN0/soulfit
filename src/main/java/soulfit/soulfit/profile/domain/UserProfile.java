package soulfit.soulfit.profile.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private UserAuth userAuth;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private MbtiType mbti;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(length = 1000)
    private String bio;

    private String region;
    private Double latitude;
    private Double longitude;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonalityKeyword> personalityKeywords = new ArrayList<>();

    @OneToOne(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private PhotoAlbum photoAlbum;

    public UserProfile(UserAuth userAuth, LocalDate birthDate, Gender gender, MbtiType mbti, String profileImageUrl, String bio) {
        this.userAuth = userAuth;
        this.birthDate = birthDate;
        this.gender = gender;
        this.mbti = mbti;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
    }

    public UserProfile(UserAuth userAuth, LocalDate birthDate, Gender gender) {
        this.userAuth = userAuth;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public void addKeyword(PersonalityKeyword keyword) {
        personalityKeywords.add(keyword);
        keyword.setUserProfile(this);
    }

    public void setAlbum(PhotoAlbum album) {
        this.photoAlbum = album;
        album.setUserProfile(this);
    }
}
