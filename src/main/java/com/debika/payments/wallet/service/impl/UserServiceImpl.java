package com.debika.payments.wallet.service.impl;

import com.debika.payments.wallet.exception.DuplicateResourceException;
import com.debika.payments.wallet.model.User;
import com.debika.payments.wallet.repository.UserRepository;
import com.debika.payments.wallet.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        checkForDuplicates(user);
        return userRepository.save(user);
    }

    private void checkForDuplicates(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
    }
}