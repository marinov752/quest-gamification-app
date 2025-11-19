package com.questgamification.repository;

import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.QuestType;
import com.questgamification.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuestRepository extends JpaRepository<Quest, UUID> {
    List<Quest> findByUser(User user);
    List<Quest> findByUserAndStatus(User user, QuestStatus status);
    List<Quest> findByUserAndQuestType(User user, QuestType questType);
    List<Quest> findByStatusAndEndDateBefore(QuestStatus status, LocalDate date);
}

