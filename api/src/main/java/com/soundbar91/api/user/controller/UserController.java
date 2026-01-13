package com.soundbar91.api.user.controller;

import com.soundbar91.domain.user.entity.User;
import com.soundbar91.domain.user.service.UserService;
import com.soundbar91.api.user.dto.request.CreateUserRequest;
import com.soundbar91.api.user.dto.request.UpdateUserRequest;
import com.soundbar91.api.user.dto.response.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 API Controller
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 사용자 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getRole()
        );
        return UserResponse.from(user);
    }

    /**
     * 사용자 조회
     */
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return UserResponse.from(user);
    }

    /**
     * 모든 사용자 조회
     */
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 프로필 수정
     */
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        User user = userService.updateUserProfile(id, request.getName(), request.getPhoneNumber());
        return UserResponse.from(user);
    }

    /**
     * 사용자 삭제
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
