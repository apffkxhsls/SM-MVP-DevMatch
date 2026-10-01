package com.devmate.profile;

import com.devmate.common.enums.ProjectGoal;
import com.devmate.common.enums.Role;
import com.devmate.common.enums.Skill;
import com.devmate.member.Member;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @ElementCollection
    @CollectionTable(
            name = "profile_skills",
            joinColumns = @JoinColumn(name = "profile_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"profile_id", "skill"}
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "skill", nullable = false, length = 30)
    private Set<Skill> skills = new HashSet<>();

    @Column(nullable = false)
    private Integer weeklyHours;

    @ElementCollection
    @CollectionTable(
            name = "profile_available_slots",
            joinColumns = @JoinColumn(name = "profile_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"profile_id", "slot"}
            )
    )
    @Column(name = "slot", nullable = false)
    private Set<Integer> availableSlots = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectGoal goal;

    @Column(length = 100)
    private String region;

    @Column(length = 2000)
    private String portfolioUrl;

    @Column(length = 1000)
    private String introduction;

    protected Profile() {
    }

    public Profile(Member member) {
        this.member = member;
    }

    /**
     * 검증된 입력값으로 프로필 내용을 변경한다.
     * 프로필 소유자는 변경하지 않는다.
     */
    public void update(
            Role role,
            Set<Skill> skills,
            Integer weeklyHours,
            Set<Integer> availableSlots,
            ProjectGoal goal,
            String region,
            String portfolioUrl,
            String introduction
    ) {
        // 전달받은 컬렉션과 내부 컬렉션이 같아도 안전하도록 복사한다.
        Set<Skill> newSkills = new HashSet<>(skills);
        Set<Integer> newSlots = new HashSet<>(availableSlots);

        this.role = role;
        this.weeklyHours = weeklyHours;
        this.goal = goal;
        this.region = region;
        this.portfolioUrl = portfolioUrl;
        this.introduction = introduction;

        this.skills.clear();
        this.skills.addAll(newSkills);

        this.availableSlots.clear();
        this.availableSlots.addAll(newSlots);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Role getRole() {
        return role;
    }

    public Set<Skill> getSkills() {
        return Collections.unmodifiableSet(skills);
    }

    public Integer getWeeklyHours() {
        return weeklyHours;
    }

    public Set<Integer> getAvailableSlots() {
        return Collections.unmodifiableSet(availableSlots);
    }

    public ProjectGoal getGoal() {
        return goal;
    }

    public String getRegion() {
        return region;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public String getIntroduction() {
        return introduction;
    }
}