package com.soundbar91.user.service;

import com.soundbar91.common.exception.BusinessException;
import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.test.fixture.UserFixture;
import com.soundbar91.user.domain.entity.User;
import com.soundbar91.user.domain.event.UserCreatedEvent;
import com.soundbar91.user.domain.repository.UserRepository;
import com.soundbar91.user.domain.vo.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("createUser 메서드")
    class CreateUser {

        @Test
        @DisplayName("유효한 정보로 사용자를 생성하면 저장된 사용자를 반환한다")
        void createUser_WithValidInfo_ReturnsUser() {
            // given
            String name = "테스트 사용자";
            String email = "test@example.com";
            String phoneNumber = "010-1234-5678";
            UserRole role = UserRole.USER;

            User expectedUser = UserFixture.create()
                    .withId(1L)
                    .withName(name)
                    .withEmail(email)
                    .withPhoneNumber(phoneNumber)
                    .withRole(role)
                    .build();

            given(userRepository.existsByEmail(email)).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(expectedUser);

            // when
            User result = userService.createUser(name, email, phoneNumber, role);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);
            assertThat(result.getRole()).isEqualTo(role);

            then(userRepository).should().existsByEmail(email);
            then(userRepository).should().save(any(User.class));
            then(eventPublisher).should().publishEvent(any(UserCreatedEvent.class));
        }

        @Test
        @DisplayName("중복된 이메일로 사용자를 생성하면 예외가 발생한다")
        void createUser_WithDuplicateEmail_ThrowsException() {
            // given
            String email = "duplicate@example.com";
            given(userRepository.existsByEmail(email)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.createUser("이름", email, "010-1234-5678", UserRole.USER))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 존재하는 이메일");

            then(userRepository).should(never()).save(any(User.class));
            then(eventPublisher).should(never()).publishEvent(any());
        }

        @Test
        @DisplayName("사용자 생성 시 UserCreatedEvent가 발행된다")
        void createUser_PublishesUserCreatedEvent() {
            // given
            String email = "event@example.com";
            User savedUser = UserFixture.create()
                    .withId(1L)
                    .withEmail(email)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);

            // when
            userService.createUser("이름", email, "010-1234-5678", UserRole.USER);

            // then
            then(eventPublisher).should().publishEvent(eventCaptor.capture());
            UserCreatedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getUserId()).isEqualTo(1L);
            assertThat(capturedEvent.getEmail()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("getUserById 메서드")
    class GetUserById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 사용자를 반환한다")
        void getUserById_WithExistingId_ReturnsUser() {
            // given
            Long userId = 1L;
            User user = UserFixture.create().withId(userId).build();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            User result = userService.getUserById(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void getUserById_WithNonExistingId_ThrowsException() {
            // given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getUserByEmail 메서드")
    class GetUserByEmail {

        @Test
        @DisplayName("존재하는 이메일로 조회하면 사용자를 반환한다")
        void getUserByEmail_WithExistingEmail_ReturnsUser() {
            // given
            String email = "test@example.com";
            User user = UserFixture.create().withEmail(email).build();
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            // when
            User result = userService.getUserByEmail(email);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회하면 예외가 발생한다")
        void getUserByEmail_WithNonExistingEmail_ThrowsException() {
            // given
            String email = "nonexistent@example.com";
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserByEmail(email))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getAllUsers 메서드")
    class GetAllUsers {

        @Test
        @DisplayName("모든 사용자를 조회하면 사용자 목록을 반환한다")
        void getAllUsers_ReturnsUserList() {
            // given
            List<User> users = List.of(
                    UserFixture.create().withId(1L).withEmail("user1@example.com").build(),
                    UserFixture.create().withId(2L).withEmail("user2@example.com").build()
            );
            given(userRepository.findAll()).willReturn(users);

            // when
            List<User> result = userService.getAllUsers();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("사용자가 없으면 빈 목록을 반환한다")
        void getAllUsers_WhenNoUsers_ReturnsEmptyList() {
            // given
            given(userRepository.findAll()).willReturn(List.of());

            // when
            List<User> result = userService.getAllUsers();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateUserProfile 메서드")
    class UpdateUserProfile {

        @Test
        @DisplayName("유효한 정보로 프로필을 수정하면 수정된 사용자를 반환한다")
        void updateUserProfile_WithValidInfo_ReturnsUpdatedUser() {
            // given
            Long userId = 1L;
            String newName = "수정된 이름";
            String newPhoneNumber = "010-9999-8888";

            User existingUser = UserFixture.create()
                    .withId(userId)
                    .withName("기존 이름")
                    .withPhoneNumber("010-1111-2222")
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

            // when
            User result = userService.updateUserProfile(userId, newName, newPhoneNumber);

            // then
            assertThat(result.getName()).isEqualTo(newName);
            assertThat(result.getPhoneNumber()).isEqualTo(newPhoneNumber);
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 프로필을 수정하면 예외가 발생한다")
        void updateUserProfile_WithNonExistingUser_ThrowsException() {
            // given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateUserProfile(userId, "이름", "010-1234-5678"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateUserRole 메서드")
    class UpdateUserRole {

        @Test
        @DisplayName("유효한 역할로 변경하면 수정된 사용자를 반환한다")
        void updateUserRole_WithValidRole_ReturnsUpdatedUser() {
            // given
            Long userId = 1L;
            UserRole newRole = UserRole.SELLER;

            User existingUser = UserFixture.create()
                    .withId(userId)
                    .withRole(UserRole.USER)
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

            // when
            User result = userService.updateUserRole(userId, newRole);

            // then
            assertThat(result.getRole()).isEqualTo(newRole);
        }
    }

    @Nested
    @DisplayName("deleteUser 메서드")
    class DeleteUser {

        @Test
        @DisplayName("존재하는 사용자를 삭제하면 정상적으로 삭제된다")
        void deleteUser_WithExistingUser_DeletesSuccessfully() {
            // given
            Long userId = 1L;
            User existingUser = UserFixture.create().withId(userId).build();
            given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

            // when
            userService.deleteUser(userId);

            // then
            then(userRepository).should().delete(existingUser);
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 삭제하면 예외가 발생한다")
        void deleteUser_WithNonExistingUser_ThrowsException() {
            // given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(userId))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("existsById 메서드")
    class ExistsById {

        @Test
        @DisplayName("존재하는 사용자 ID이면 true를 반환한다")
        void existsById_WithExistingUser_ReturnsTrue() {
            // given
            Long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.of(UserFixture.createDefault()));

            // when
            boolean result = userService.existsById(userId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID이면 false를 반환한다")
        void existsById_WithNonExistingUser_ReturnsFalse() {
            // given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when
            boolean result = userService.existsById(userId);

            // then
            assertThat(result).isFalse();
        }
    }
}
