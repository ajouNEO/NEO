package com.neo.back.authorization.repository;

import com.neo.back.authorization.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    //jpa 구문 존재하는지
    Boolean existsByUsername(String username);
    Boolean existsByname(String name);

    User save(User user);
    Optional<User> findById(Long id);
    User findByUsername(String username);
    List<User> findAll();

    User findByName(String participantName);
}
