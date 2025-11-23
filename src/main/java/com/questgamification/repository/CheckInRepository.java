package com.questgamification.repository;

import com.questgamification.domain.entity.CheckIn;
import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {
    
    List<CheckIn> findByQuestAndUserOrderByCheckInDateDesc(Quest quest, User user);
    
    Optional<CheckIn> findByQuestAndUserAndCheckInDate(Quest quest, User user, LocalDate date);
    
    long countByQuestAndUser(Quest quest, User user);
    
    List<CheckIn> findByQuestAndUser(Quest quest, User user);
}

