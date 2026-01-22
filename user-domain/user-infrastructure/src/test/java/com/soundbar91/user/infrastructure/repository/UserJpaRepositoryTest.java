package com.soundbar91.user.infrastructure.repository;

import com.soundbar91.user.domain.entity.User;
import com.soundbar91.user.domain.vo.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = UserJpaRepositoryTest.TestConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("UserJpaRepository 테스트")
class UserJpaRepositoryTest {

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.soundbar91.user.domain.entity")
    @EnableJpaRepositories(basePackages = "com.soundbar91.user.infrastructure.repository")
    static class TestConfig {}

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Nested
    @DisplayName("save 메서드")
    class Save {

        @Test
        @DisplayName("새로운 사용자를 저장하면 ID가 생성된다")
        void save_NewUser_GeneratesId() {
            // given
            User user = new User("테스트 사용자", "test@example.com", "010-1234-5678", UserRole.USER);

            // when
            User savedUser = userJpaRepository.save(user);

            // then
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getName()).isEqualTo("테스트 사용자");
            assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
            assertThat(savedUser.getCreatedAt()).isNotNull();
            assertThat(savedUser.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById 메서드")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 사용자를 반환한다")
        void findById_WithExistingId_ReturnsUser() {
            // given
            User user = new User("테스트 사용자", "test@example.com", "010-1234-5678", UserRole.USER);
            User persistedUser = userJpaRepository.save(user);

            // when
            Optional<User> found = userJpaRepository.findById(persistedUser.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void findById_WithNonExistingId_ReturnsEmpty() {
            // when
            Optional<User> found = userJpaRepository.findById(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail 메서드")
    class FindByEmail {

        @Test
        @DisplayName("존재하는 이메일로 조회하면 사용자를 반환한다")
        void findByEmail_WithExistingEmail_ReturnsUser() {
            // given
            String email = "unique@example.com";
            User user = new User("테스트 사용자", email, "010-1234-5678", UserRole.USER);
            userJpaRepository.save(user);

            // when
            Optional<User> found = userJpaRepository.findByEmail(email);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("테스트 사용자");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환한다")
        void findByEmail_WithNonExistingEmail_ReturnsEmpty() {
            // when
            Optional<User> found = userJpaRepository.findByEmail("nonexistent@example.com");

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmail 메서드")
    class ExistsByEmail {

        @Test
        @DisplayName("존재하는 이메일이면 true를 반환한다")
        void existsByEmail_WithExistingEmail_ReturnsTrue() {
            // given
            String email = "exists@example.com";
            User user = new User("테스트 사용자", email, "010-1234-5678", UserRole.USER);
            userJpaRepository.save(user);

            // when
            boolean exists = userJpaRepository.existsByEmail(email);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 false를 반환한다")
        void existsByEmail_WithNonExistingEmail_ReturnsFalse() {
            // when
            boolean exists = userJpaRepository.existsByEmail("notexists@example.com");

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findAll 메서드")
    class FindAll {

        @Test
        @DisplayName("모든 사용자를 조회한다")
        void findAll_ReturnsAllUsers() {
            // given
            User user1 = new User("사용자1", "user1@example.com", "010-1111-1111", UserRole.USER);
            User user2 = new User("사용자2", "user2@example.com", "010-2222-2222", UserRole.SELLER);
            userJpaRepository.save(user1);
            userJpaRepository.save(user2);

            // when
            var users = userJpaRepository.findAll();

            // then
            assertThat(users).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class Delete {

        @Test
        @DisplayName("사용자를 삭제하면 조회되지 않는다")
        void delete_RemovesUser() {
            // given
            User user = new User("삭제될 사용자", "delete@example.com", "010-1234-5678", UserRole.USER);
            User persistedUser = userJpaRepository.save(user);
            Long userId = persistedUser.getId();

            // when
            userJpaRepository.delete(persistedUser);
            userJpaRepository.flush();

            // then
            Optional<User> found = userJpaRepository.findById(userId);
            assertThat(found).isEmpty();
        }
    }
}
