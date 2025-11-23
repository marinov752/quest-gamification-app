package com.questgamification.repository;

import com.questgamification.domain.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    @EntityGraph(attributePaths = {"achievements", "roles"})
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @EntityGraph(attributePaths = {"achievements", "roles"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithAchievements(@Param("id") UUID id);
}

