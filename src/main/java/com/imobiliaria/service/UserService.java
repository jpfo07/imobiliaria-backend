package com.imobiliaria.service;

import com.imobiliaria.dto.UserDTO;
import com.imobiliaria.entity.User;
import com.imobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    @Transactional
    public User updateProfile(String email, UserDTO dto) {
        User user = getByEmail(email);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getPhotoUrl() != null) {
            user.setPhotoUrl(dto.getPhotoUrl());
        }
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            if (dto.getCurrentPassword() == null ||
                !passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Senha atual incorreta");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * CORRIGIDO: DISTINCT para evitar duplicatas quando Hibernate faz JOIN
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAllDistinct();
    }
}