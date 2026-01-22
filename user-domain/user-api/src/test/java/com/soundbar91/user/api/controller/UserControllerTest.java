package com.soundbar91.user.api.controller;

import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.test.fixture.UserFixture;
import com.soundbar91.user.api.dto.request.CreateUserRequest;
import com.soundbar91.user.api.dto.request.UpdateUserRequest;
import com.soundbar91.user.domain.entity.User;
import com.soundbar91.user.domain.vo.UserRole;
import com.soundbar91.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = UserControllerTest.TestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController 테스트")
class UserControllerTest {

    @EnableAutoConfiguration
    @ComponentScan(basePackages = "com.soundbar91.user.api")
    static class TestConfig {

        @RestControllerAdvice
        static class TestExceptionHandler {
            @ExceptionHandler(NotFoundException.class)
            public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("POST /api/v2/users")
    class CreateUser {

        @Test
        @DisplayName("유효한 요청으로 사용자를 생성하면 201 Created를 반환한다")
        void createUser_WithValidRequest_Returns201() throws Exception {
            // given
            CreateUserRequest request = new CreateUserRequest(
                    "테스트 사용자", "test@example.com", "010-1234-5678", UserRole.USER);

            User createdUser = UserFixture.create()
                    .withId(1L)
                    .withName("테스트 사용자")
                    .withEmail("test@example.com")
                    .withPhoneNumber("010-1234-5678")
                    .withRole(UserRole.USER)
                    .build();

            given(userService.createUser(any(), any(), any(), any())).willReturn(createdUser);

            // when & then
            mockMvc.perform(post("/api/v2/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("테스트 사용자"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/users/{id}")
    class GetUser {

        @Test
        @DisplayName("존재하는 ID로 조회하면 200 OK와 사용자 정보를 반환한다")
        void getUser_WithExistingId_Returns200() throws Exception {
            // given
            Long userId = 1L;
            User user = UserFixture.create()
                    .withId(userId)
                    .withName("테스트 사용자")
                    .withEmail("test@example.com")
                    .build();

            given(userService.getUserById(userId)).willReturn(user);

            // when & then
            mockMvc.perform(get("/api/v2/users/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.name").value("테스트 사용자"));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 404 Not Found를 반환한다")
        void getUser_WithNonExistingId_Returns404() throws Exception {
            // given
            Long userId = 999L;
            given(userService.getUserById(userId))
                    .willThrow(new NotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

            // when & then
            mockMvc.perform(get("/api/v2/users/{id}", userId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v2/users")
    class GetAllUsers {

        @Test
        @DisplayName("모든 사용자를 조회하면 200 OK와 사용자 목록을 반환한다")
        void getAllUsers_Returns200WithList() throws Exception {
            // given
            List<User> users = List.of(
                    UserFixture.create().withId(1L).withEmail("user1@example.com").build(),
                    UserFixture.create().withId(2L).withEmail("user2@example.com").build()
            );

            given(userService.getAllUsers()).willReturn(users);

            // when & then
            mockMvc.perform(get("/api/v2/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));
        }

        @Test
        @DisplayName("사용자가 없으면 빈 배열을 반환한다")
        void getAllUsers_WhenEmpty_ReturnsEmptyArray() throws Exception {
            // given
            given(userService.getAllUsers()).willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/v2/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("PUT /api/v2/users/{id}")
    class UpdateUser {

        @Test
        @DisplayName("유효한 요청으로 사용자를 수정하면 200 OK를 반환한다")
        void updateUser_WithValidRequest_Returns200() throws Exception {
            // given
            Long userId = 1L;
            UpdateUserRequest request = new UpdateUserRequest("수정된 이름", "010-9999-8888");

            User updatedUser = UserFixture.create()
                    .withId(userId)
                    .withName("수정된 이름")
                    .withPhoneNumber("010-9999-8888")
                    .build();

            given(userService.updateUserProfile(eq(userId), any(), any())).willReturn(updatedUser);

            // when & then
            mockMvc.perform(put("/api/v2/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("수정된 이름"))
                    .andExpect(jsonPath("$.phoneNumber").value("010-9999-8888"));
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 수정하면 404 Not Found를 반환한다")
        void updateUser_WithNonExistingUser_Returns404() throws Exception {
            // given
            Long userId = 999L;
            UpdateUserRequest request = new UpdateUserRequest("수정된 이름", "010-9999-8888");

            given(userService.updateUserProfile(eq(userId), any(), any()))
                    .willThrow(new NotFoundException("사용자를 찾을 수 없습니다"));

            // when & then
            mockMvc.perform(put("/api/v2/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v2/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("존재하는 사용자를 삭제하면 204 No Content를 반환한다")
        void deleteUser_WithExistingUser_Returns204() throws Exception {
            // given
            Long userId = 1L;
            willDoNothing().given(userService).deleteUser(userId);

            // when & then
            mockMvc.perform(delete("/api/v2/users/{id}", userId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 삭제하면 404 Not Found를 반환한다")
        void deleteUser_WithNonExistingUser_Returns404() throws Exception {
            // given
            Long userId = 999L;
            willThrow(new NotFoundException("사용자를 찾을 수 없습니다"))
                    .given(userService).deleteUser(userId);

            // when & then
            mockMvc.perform(delete("/api/v2/users/{id}", userId))
                    .andExpect(status().isNotFound());
        }
    }
}
