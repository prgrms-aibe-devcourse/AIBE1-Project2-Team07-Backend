package org.lucky0111.pettalk.controller.tag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.service.tag.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token"
)
@Tag(name = "태그 API", description = "태그 관련 API 엔드포인트")
public class TagController {

    private final TagService tagService;

    @GetMapping("/open")
    @Operation(
            summary = "태그 목록 조회",
            description = "태그 목록을 조회합니다."
    )
    public ResponseEntity<List<org.lucky0111.pettalk.domain.entity.common.Tag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }
}
