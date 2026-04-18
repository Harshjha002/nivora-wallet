package com.nivora.pay.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.nivora.pay.entities.User;
import com.nivora.pay.services.UserService;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody User user){
        User newUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);

    }

    @GetMapping("/id/{id}") 
    public ResponseEntity<User> getUserByID(@PathVariable Long id){
        User user = userService.getUuseByID(id);
        return ResponseEntity.ok(user);

    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<User>> getUsersByName(@PathVariable String name){
        List<User> users = userService.getUserByName(name);
        return ResponseEntity.ok(users);

    }
    
}
