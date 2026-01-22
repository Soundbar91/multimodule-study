package com.soundbar91.test.fixture;

import com.soundbar91.user.domain.entity.User;
import com.soundbar91.user.domain.vo.UserRole;

import java.lang.reflect.Field;

/**
 * User 엔티티 테스트 픽스처
 */
public class UserFixture {

    private static final String DEFAULT_NAME = "테스트 사용자";
    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final String DEFAULT_PHONE = "010-1234-5678";
    private static final UserRole DEFAULT_ROLE = UserRole.USER;

    private String name = DEFAULT_NAME;
    private String email = DEFAULT_EMAIL;
    private String phoneNumber = DEFAULT_PHONE;
    private UserRole role = DEFAULT_ROLE;
    private Long id = null;

    private UserFixture() {}

    public static UserFixture create() {
        return new UserFixture();
    }

    public static User createDefault() {
        return new User(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PHONE, DEFAULT_ROLE);
    }

    public static User createAdmin() {
        return new User("관리자", "admin@example.com", "010-0000-0000", UserRole.ADMIN);
    }

    public static User createSeller() {
        return new User("판매자", "seller@example.com", "010-1111-1111", UserRole.SELLER);
    }

    public UserFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public UserFixture withName(String name) {
        this.name = name;
        return this;
    }

    public UserFixture withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserFixture withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public UserFixture withRole(UserRole role) {
        this.role = role;
        return this;
    }

    public User build() {
        User user = new User(name, email, phoneNumber, role);
        if (id != null) {
            setId(user, id);
        }
        return user;
    }

    /**
     * 리플렉션을 사용하여 ID 설정 (테스트 전용)
     */
    private static void setId(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set ID via reflection", e);
        }
    }
}
