package com.questgamification.service;

import com.questgamification.domain.dto.UserRegistrationDto;
import com.questgamification.domain.entity.Role;
import com.questgamification.domain.entity.User;
import com.questgamification.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("testuser");
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setConfirmPassword("password123");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.count()).thenReturn(1L); // Not the first user
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        User result = userService.registerUser(registrationDto);

        assertNotNull(result);
        assertTrue(result.getRoles().contains(Role.USER));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_PasswordsDoNotMatch() {
        registrationDto.setConfirmPassword("differentPassword");

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(registrationDto);
        });
    }

    @Test
    void testRegisterUser_UsernameExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(registrationDto);
        });
    }
}

