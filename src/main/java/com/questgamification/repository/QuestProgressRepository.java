package com.questgamification.repository;

import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestProgress;
import com.questgamification.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestProgressRepository extends JpaRepository<QuestProgress, UUID> {
    Optional<QuestProgress> findByQuestAndUser(Quest quest, User user);
    List<QuestProgress> findByUser(User user);
}

