package com.devmate.matching;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record MatchResult(
        MatchStatus status,
        Integer score,
        List<String> reasons,
        Set<Integer> commonSlots
) {

    public MatchResult {
        Objects.requireNonNull(status, "판정 상태가 필요합니다.");
        Objects.requireNonNull(reasons, "판정 사유 목록이 필요합니다.");
        Objects.requireNonNull(commonSlots, "공통 시간대 목록이 필요합니다.");

        if (status == MatchStatus.PASS) {
            if (score == null || score < 0 || score > 100) {
                throw new IllegalArgumentException("PASS 점수는 0~100 사이어야 합니다.");
            }
        } else if (score != null) {
            throw new IllegalArgumentException("FAIL과 UNKNOWN은 점수를 가질 수 없습니다.");
        }

        reasons = List.copyOf(reasons);
        commonSlots = Set.copyOf(commonSlots);
    }
}
