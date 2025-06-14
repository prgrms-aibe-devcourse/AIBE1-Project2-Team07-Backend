package org.lucky0111.pettalk.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.lucky0111.pettalk.config.TestSecurityConfig;
import org.lucky0111.pettalk.controller.common.ControllerTest;
import org.lucky0111.pettalk.domain.common.UserRole;
import org.lucky0111.pettalk.domain.dto.user.UserResponseDTO;
import org.lucky0111.pettalk.domain.dto.user.UserUpdateDTO;
import org.lucky0111.pettalk.service.user.UserService;
import org.lucky0111.pettalk.util.auth.JwtFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
)
@Import(TestSecurityConfig.class)
public class UserControllerTest extends ControllerTest {
    @MockitoBean
    private UserService userService;

    private UserResponseDTO expectedUserResponse;

    @BeforeEach
    void setUp() {
        expectedUserResponse = new UserResponseDTO(
                "테스트유저",
                "testuser",
                "test@example.com",
                "http://example.com/profile.jpg",
                UserRole.USER
        );
    }

    @Nested
    @DisplayName("인증된 유저가")
    @WithMockUser
    public class AuthenticatedUser {
        @Test
        @DisplayName("사용자 정보를 조회할 때 200 응답과 사용자 정보가 반환되어야 한다.")
        void getUser_whenValidRequest_thenReturnsUserInfo() throws Exception {
            // Given
            when(userService.getUserById()).thenReturn(expectedUserResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(expectedUserResponse.name()))
                    .andExpect(jsonPath("$.nickname").value(expectedUserResponse.nickname()))
                    .andExpect(jsonPath("$.email").value(expectedUserResponse.email()))
                    .andExpect(jsonPath("$.profileImageUrl").value(expectedUserResponse.profileImageUrl()))
                    .andExpect(jsonPath("$.userRole").value(expectedUserResponse.userRole().name()))
                    .andDo(print());

            verify(userService).getUserById();
        }

        @Test
        @DisplayName("사용자 정보를 수정하면 200 응답과 수정된 사용자 정보가 반환되어야 한다.")
        void updateUser_whenValidRequest_thenReturnsUpdatedUserInfo() throws Exception {
            // Given
            UserUpdateDTO request = new UserUpdateDTO(
                    "수정된이름",
                    "수정된닉네임",
                    "http://example.com/updated-profile.jpg"
            );

            UserUpdateDTO expectedResponse = new UserUpdateDTO(
                    "수정된이름",
                    "수정된닉네임",
                    "http://example.com/updated-profile.jpg"
            );

            when(userService.updateUser(request)).thenReturn(expectedResponse);

            // When & Then
            mockMvc.perform(put("/api/v1/users/update")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(expectedResponse.name()))
                    .andExpect(jsonPath("$.nickname").value(expectedResponse.nickname()))
                    .andExpect(jsonPath("$.profileImageUrl").value(expectedResponse.profileImageUrl()))
                    .andDo(print());
        }

        @Test
        @DisplayName("사용자 프로필 이미지를 수정하면 200 응답과 수정된 사용자 정보가 반환되어야 한다.")
        void updateUserImage_whenValidRequest_thenReturnsUpdatedUserInfo() throws Exception {
            // Given
            UserUpdateDTO expectedResponse = new UserUpdateDTO(
                    "테스트유저",
                    "testuser",
                    "http://example.com/updated-profile.jpg"
            );

            MockMultipartFile mockFile = new MockMultipartFile(
                    "file",
                    "profile.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(userService.updateUserImage(mockFile)).thenReturn(expectedResponse);

            // When & Then
            mockMvc.perform(multipart("/api/v1/users/updateImage")
                            .file(mockFile)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(expectedResponse.name()))
                    .andExpect(jsonPath("$.nickname").value(expectedResponse.nickname()))
                    .andExpect(jsonPath("$.profileImageUrl").value(expectedResponse.profileImageUrl()))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("인증되지 않은 유저가")
    @WithAnonymousUser
    public class UnauthenticatedUser {
        @Test
        @DisplayName("사용자 정보를 조회할 때 403 응답이 반환되어야 한다.")
        void getUser_whenNotAuthenticated_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }
    }
}
