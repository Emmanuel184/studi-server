package com.studi.server.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.studi.server.model.AppUser;
import com.studi.server.repository.UserRepository;

@Component
public class DataLoaderRunner implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoaderRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        createUserIfMissing("emmanuel");
        createUserIfMissing("josetta");
    }

    private void createUserIfMissing(String username) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        userRepository.save(new AppUser(username, passwordEncoder.encode(DEFAULT_PASSWORD)));
    }
}
