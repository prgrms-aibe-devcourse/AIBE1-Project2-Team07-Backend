package org.lucky0111.pettalk.domain.common;

public enum Status {
    PENDING("신청중"),
    APPROVED("승인"),
    REJECTED("미승인");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
