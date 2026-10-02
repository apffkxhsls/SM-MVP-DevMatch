package com.devmate.matching;

public enum MatchStatus {
    PASS,       // 필수 조건 모두 충족
    FAIL,       // 명확한 필수 조건 불일치
    UNKNOWN     // 불일치는 없지만 필수 정보 부족
}
