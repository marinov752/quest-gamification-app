package com.questgamification.integration;

import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.QuestType;
import com.questgamification.domain.entity.User;
import com.questgamification.repository.QuestRepository;
import com.questgamification.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class QuestIntegrationTest {

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateAndFindQuest() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setLevel(1);
        user.setExperiencePoints(0L);
        user = userRepository.save(user);

        Quest quest = new Quest();
        quest.setTitle("Test Quest");
        quest.setDescription("Test Description");
        quest.setQuestType(QuestType.DAILY);
        quest.setStatus(QuestStatus.ACTIVE);
        quest.setExperienceReward(100L);
        quest.setStartDate(LocalDate.now());
        quest.setEndDate(LocalDate.now().plusDays(1));
        quest.setUser(user);

        Quest savedQuest = questRepository.save(quest);

        assertNotNull(savedQuest.getId());
        assertEquals("Test Quest", savedQuest.getTitle());
        assertEquals(QuestStatus.ACTIVE, savedQuest.getStatus());
    }
}

