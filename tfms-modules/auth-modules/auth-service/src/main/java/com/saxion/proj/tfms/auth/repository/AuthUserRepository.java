package com.saxion.proj.tfms.auth.repository;

import com.saxion.proj.tfms.commons.model.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<UserDao, Long> {
    
    Optional<UserDao> findByEmail(String email);
    
    @Query("SELECT u FROM UserDao u WHERE u.email = :email AND u.active = true")
    Optional<UserDao> findActiveByEmail(@Param("email") String email);

    boolean existsByEmail(String email);
    
    Optional<UserDao> findByApiKey(String apiKey);
}
