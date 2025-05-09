package org.lucky0111.pettalk.domain.dto.trainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TrainerProfileUpdateDTO(
        // 기본 정보 (Trainer 엔티티 필드에 해당)
        @NotBlank String title,
        @NotBlank String representativeCareer, // 대표경력
        @NotBlank String specializationText, // 전문분야
        @NotBlank String visitingAreas, // 방문지역
//        @NotNull @PositiveOrZero Integer experienceYears, 연차는 보류

        // 가격 정보 (ServiceFee 목록)
        @NotNull List<ServiceFeeUpdateDTO> serviceFees,

        // 전문 분야 (Tag 이름 목록)
        // 추후 MCP 활용하여 프로필 수정 내용을 기반으로 Tag 선정하여 넣어달라고 할 예정.
        List<String> tags, // 또는 Set<String>으로 변경 가능 (중복 이름 방지 시)


        @NotBlank String introduction
        // 사진 파일 자체는 @RequestPart로 별도로 받습니다. 이 DTO에는 파일 정보(URL 등)만 포함될 수 있습니다.
        // 현재 설계는 사진 파일 자체를 RequestPart로 받으므로 DTO에는 파일 정보 필드 불필요.
) {
}
