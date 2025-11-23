package com.questgamification.service;

import com.questgamification.domain.dto.RewardCreateDto;
import com.questgamification.domain.entity.Reward;
import com.questgamification.domain.entity.User;
import com.questgamification.repository.RewardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RewardService {

    private static final Logger logger = LoggerFactory.getLogger(RewardService.class);
    private final RewardRepository rewardRepository;

    public RewardService(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    @Cacheable(value = "rewards", key = "#level")
    public List<Reward> getAvailableRewards(Integer level) {
        return rewardRepository.findByRequiredLevelLessThanEqualOrderByRequiredLevelAsc(level);
    }

    public List<Reward> getAllRewards() {
        logger.debug("Fetching all rewards");
        return rewardRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "rewards", allEntries = true)
    public Reward createReward(RewardCreateDto rewardDto) {
        logger.info("Creating reward: {}", rewardDto.getName());
        
        Reward reward = new Reward();
        reward.setName(rewardDto.getName());
        reward.setDescription(rewardDto.getDescription());
        reward.setRequiredLevel(rewardDto.getRequiredLevel());
        reward.setRequiredExperience(rewardDto.getRequiredExperience());
        reward.setIsClaimed(false);
        
        Reward saved = rewardRepository.save(reward);
        logger.info("Reward created successfully with ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    @CacheEvict(value = "rewards", allEntries = true)
    public Reward updateReward(UUID id, RewardCreateDto rewardDto) {
        logger.info("Updating reward: {}", id);
        
        Reward reward = rewardRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reward not found"));
        
        reward.setName(rewardDto.getName());
        reward.setDescription(rewardDto.getDescription());
        reward.setRequiredLevel(rewardDto.getRequiredLevel());
        reward.setRequiredExperience(rewardDto.getRequiredExperience());
        
        Reward saved = rewardRepository.save(reward);
        logger.info("Reward updated successfully: {}", saved.getId());
        return saved;
    }

    @Transactional
    @CacheEvict(value = "rewards", allEntries = true)
    public void deleteReward(UUID id) {
        logger.info("Deleting reward: {}", id);
        Reward reward = rewardRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reward not found"));
        rewardRepository.delete(reward);
        logger.info("Reward deleted successfully: {}", id);
    }

    public Reward findById(UUID id) {
        return rewardRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reward not found"));
    }

    @Transactional
    @CacheEvict(value = {"rewards", "users"}, allEntries = true)
    public Reward claimReward(UUID rewardId, User user) {
        logger.info("User {} attempting to claim reward {}", user.getUsername(), rewardId);
        
        Reward reward = rewardRepository.findById(rewardId)
            .orElseThrow(() -> new IllegalArgumentException("Reward not found"));

        if (user.getLevel() < reward.getRequiredLevel()) {
            throw new IllegalArgumentException("User level is too low to claim this reward");
        }

        if (user.getExperiencePoints() < reward.getRequiredExperience()) {
            throw new IllegalArgumentException("User does not have enough experience to claim this reward");
        }

        if (user.getClaimedRewards().contains(reward)) {
            throw new IllegalArgumentException("Reward already claimed");
        }

        user.getClaimedRewards().add(reward);
        reward.getUsers().add(user);
        
        rewardRepository.save(reward);
        
        logger.info("Reward {} claimed successfully by user {}", rewardId, user.getUsername());
        return reward;
    }
}

