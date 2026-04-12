package com.ween.scheduler;

import com.ween.entity.CoinTransaction;
import com.ween.entity.LeaderboardEntry;
import com.ween.entity.User;
import com.ween.enums.CoinReason;
import com.ween.enums.LeaderboardPeriod;
import com.ween.enums.LeaderboardScope;
import com.ween.repository.CoinTransactionRepository;
import com.ween.repository.LeaderboardEntryRepository;
import com.ween.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardScheduler {

    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void recalculateLeaderboards() {
        log.info("Starting leaderboard recalculation");
        try {
            recalculateByPeriodAndScope(LeaderboardPeriod.MONTHLY, LeaderboardScope.GLOBAL);
            recalculateByPeriodAndScope(LeaderboardPeriod.QUARTERLY, LeaderboardScope.GLOBAL);
            awardMonthlyBonuses();
            log.info("Leaderboard recalculation completed successfully");
        } catch (Exception ex) {
            log.error("Failed to recalculate leaderboards", ex);
        }
    }

    private void recalculateByPeriodAndScope(LeaderboardPeriod period, LeaderboardScope scope) {
        leaderboardEntryRepository.deleteByPeriodAndScope(period, scope);

        List<User> users = userRepository.findAll();
        int rank = 1;

        for (User user : users) {
            int coinCount = calculateUserCoinCount(user.getId(), period);
            if (coinCount > 0) {
                LeaderboardEntry entry = LeaderboardEntry.builder()
                    .userId(user.getId())
                    .period(period)
                    .scope(scope)
                    .rankPosition(rank++)
                    .coinCount(coinCount)
                    .calculatedAt(LocalDateTime.now())
                    .build();
                leaderboardEntryRepository.save(entry);
            }
        }

        log.debug("Leaderboard recalculated for period: {}, scope: {}", period, scope);
    }

    private void awardMonthlyBonuses() {
        Pageable topTen = PageRequest.of(0, 10);
        var topUsers = leaderboardEntryRepository.findByPeriodAndScopeOrderByRankPositionAsc(
            LeaderboardPeriod.MONTHLY,
            LeaderboardScope.GLOBAL,
            topTen
        );

        topUsers.forEach(entry -> {
            User user = userRepository.findById(entry.getUserId()).orElse(null);
            if (user != null) {
                user.setWeenCoinBalance(user.getWeenCoinBalance() + 200);
                userRepository.save(user);

                CoinTransaction transaction = CoinTransaction.builder()
                    .userId(user.getId())
                    .amount(200)
                    .reason(CoinReason.LEADERBOARD_BONUS)
                    .relatedEntityId(null)
                    .build();
                coinTransactionRepository.save(transaction);
                log.debug("Monthly bonus awarded to user: {}", user.getId());
            }
        });
    }

    private int calculateUserCoinCount(String userId, LeaderboardPeriod period) {
        return coinTransactionRepository.sumByUserIdAndReason(userId, CoinReason.ATTENDANCE);
    }
}
