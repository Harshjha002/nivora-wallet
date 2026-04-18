package com.nivora.pay.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nivora.pay.entities.User;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {

List<User> findByNameContainingIgnoreCase(String name);
    
}