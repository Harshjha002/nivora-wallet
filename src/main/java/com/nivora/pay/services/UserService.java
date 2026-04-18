package com.nivora.pay.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nivora.pay.entities.User;
import com.nivora.pay.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {

        log.info("Creating user: {}", user.getEmail());

        User newUser = userRepository.save(user);

        log.info(
            "User created with id: {} in database Shardwallet{}",
            newUser.getId(),
            (newUser.getId() % 2 + 1)
        );

        return newUser;
    }

    public User getUuseByID(Long id){

        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    }

    public List<User> getUserByName(String name){
        return userRepository.findByNameContainingIgnoreCase(name);
    }
}