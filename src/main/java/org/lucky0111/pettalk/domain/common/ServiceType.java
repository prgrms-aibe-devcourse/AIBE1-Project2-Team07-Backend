package org.lucky0111.pettalk.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceType {
    VISIT_TRAINING("방문 교육"),
    VIDEO_TRAINING("영상 교육");

    final String description;
}
