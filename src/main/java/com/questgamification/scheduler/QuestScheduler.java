package com.questgamification.scheduler;

import com.questgamification.service.QuestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class QuestScheduler {

    private static final Logger logger = LoggerFactory.getLogger(QuestScheduler.class);
    private final QuestService questService;

    public QuestScheduler(QuestService questService) {
        this.questService = questService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void expireQuestsDaily() {
        logger.info("Running daily quest expiration job at {}", LocalDateTime.now());
        var expiredQuests = questService.findExpiredQuests();
        if (!expiredQuests.isEmpty()) {
            questService.expireQuests(expiredQuests);
            logger.info("Expired {} quests", expiredQuests.size());
        }
    }

    @Scheduled(fixedDelay = 3600000)
    public void expireQuestsHourly() {
        logger.info("Running hourly quest expiration check at {}", LocalDateTime.now());
        var expiredQuests = questService.findExpiredQuests();
        if (!expiredQuests.isEmpty()) {
            questService.expireQuests(expiredQuests);
            logger.info("Expired {} quests", expiredQuests.size());
        }
    }
}

