package com.soundbar91.integration;

import com.soundbar91.common.exception.BusinessException;
import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.user.domain.entity.User;
import com.soundbar91.user.domain.vo.UserRole;
import com.soundbar91.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User 도메인 통합 테스트")
class UserIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserService userService;

    @Nested
    @DisplayName("사용자 생애주기 테스트")
    class UserLifecycle {

        @Test
        @DisplayName("사용자를 생성하고 조회할 수 있다")
        void createAndRetrieveUser() {
            // given
            String name = "통합테스트 사용자";
            String email = "integration@example.com";
            String phoneNumber = "010-1234-5678";
            UserRole role = UserRole.USER;

            // when
            User createdUser = userService.createUser(name, email, phoneNumber, role);

            // then
            assertThat(createdUser.getId()).isNotNull();

            User foundUser = userService.getUserById(createdUser.getId());
            assertThat(foundUser.getName()).isEqualTo(name);
            assertThat(foundUser.getEmail()).isEqualTo(email);
            assertThat(foundUser.getRole()).isEqualTo(role);
        }

        @Test
        @DisplayName("사용자 프로필을 수정할 수 있다")
        void updateUserProfile() {
            // given
            User user = userService.createUser("원본 이름", "update@example.com", "010-1111-1111", UserRole.USER);
            Long userId = user.getId();

            // when
            User updatedUser = userService.updateUserProfile(userId, "수정된 이름", "010-9999-9999");

            // then
            assertThat(updatedUser.getName()).isEqualTo("수정된 이름");
            assertThat(updatedUser.getPhoneNumber()).isEqualTo("010-9999-9999");
        }

        @Test
        @DisplayName("사용자 역할을 변경할 수 있다")
        void updateUserRole() {
            // given
            User user = userService.createUser("역할변경 사용자", "role@example.com", "010-2222-2222", UserRole.USER);
            Long userId = user.getId();

            // when
            User updatedUser = userService.updateUserRole(userId, UserRole.SELLER);

            // then
            assertThat(updatedUser.getRole()).isEqualTo(UserRole.SELLER);
        }

        @Test
        @DisplayName("사용자를 삭제하면 더 이상 조회할 수 없다")
        void deleteUser() {
            // given
            User user = userService.createUser("삭제될 사용자", "delete@example.com", "010-3333-3333", UserRole.USER);
            Long userId = user.getId();

            // when
            userService.deleteUser(userId);

            // then
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("사용자 검증 테스트")
    class UserValidation {

        @Test
        @DisplayName("중복된 이메일로 사용자를 생성하면 예외가 발생한다")
        void duplicateEmailThrowsException() {
            // given
            String email = "duplicate@example.com";
            userService.createUser("첫번째 사용자", email, "010-1111-1111", UserRole.USER);

            // when & then
            assertThatThrownBy(() -> userService.createUser("두번째 사용자", email, "010-2222-2222", UserRole.USER))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 존재하는 이메일");
        }
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    class UserQuery {

        @Test
        @DisplayName("모든 사용자를 조회할 수 있다")
        void getAllUsers() {
            // given
            userService.createUser("사용자1", "user1@example.com", "010-1111-1111", UserRole.USER);
            userService.createUser("사용자2", "user2@example.com", "010-2222-2222", UserRole.SELLER);
            userService.createUser("사용자3", "user3@example.com", "010-3333-3333", UserRole.ADMIN);

            // when
            List<User> users = userService.getAllUsers();

            // then
            assertThat(users).hasSizeGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("이메일로 사용자를 조회할 수 있다")
        void getUserByEmail() {
            // given
            String email = "findbyemail@example.com";
            userService.createUser("이메일조회 사용자", email, "010-4444-4444", UserRole.USER);

            // when
            User foundUser = userService.getUserByEmail(email);

            // then
            assertThat(foundUser.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("사용자 존재 여부를 확인할 수 있다")
        void existsById() {
            // given
            User user = userService.createUser("존재확인 사용자", "exists@example.com", "010-5555-5555", UserRole.USER);

            // when & then
            assertThat(userService.existsById(user.getId())).isTrue();
            assertThat(userService.existsById(999999L)).isFalse();
        }
    }
}
