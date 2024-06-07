package com.neo.back.infoManaging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neo.back.authorization.entity.User;
import com.neo.back.infoManaging.entity.UserInquiry;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserInquiryRepository extends JpaRepository<UserInquiry, Long>{
    List<UserInquiry> findByUser(User user);
    Optional<UserInquiry> findById(Long id);
}
