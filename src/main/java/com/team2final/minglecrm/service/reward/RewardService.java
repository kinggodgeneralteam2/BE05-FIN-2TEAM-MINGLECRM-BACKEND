package com.team2final.minglecrm.service.reward;

import com.team2final.minglecrm.controller.reward.response.RewardHistoryResponse;
import com.team2final.minglecrm.controller.reward.response.RewardResponse;
import com.team2final.minglecrm.entity.reward.Reward;
import com.team2final.minglecrm.entity.reward.RewardHistory;
import com.team2final.minglecrm.persistence.repository.customer.CustomerRepository;
import com.team2final.minglecrm.persistence.repository.reward.RewardHistoryRepository;
import com.team2final.minglecrm.persistence.repository.reward.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardService {
    private final RewardRepository rewardRepository;
    private final RewardHistoryRepository rewardHistoryRepository;

    @Transactional
    public RewardResponse getReward(Long customerId) {
        Reward reward = rewardRepository.findByCustomerId(customerId);
        return RewardResponse.of(reward);
    }

    @Transactional
    public List<RewardHistoryResponse> getRewardHistories(Long customerId) {
        List<RewardHistory> rewardHistories = rewardHistoryRepository.findRewardHistoriesByCustomerId(customerId);

        return rewardHistories.stream()
                .map(RewardHistoryResponse::of)
                .collect(Collectors.toList());
    }
}
