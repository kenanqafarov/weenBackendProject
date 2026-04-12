package com.ween.scheduler;

import com.ween.entity.CoinTransaction;
import com.ween.entity.User;
import com.ween.enums.CoinReason;
import com.ween.repository.CoinTransactionRepository;
import com.ween.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnualAchievementScheduler {

    private final CoinTransactionRepository coinTransactionRepository;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 1 1 *")
    @Transactional
    public void awardAnnualAchievements() {
        log.info("Starting annual achievement awards");
        try {
            LocalDateTime yearAgo = LocalDateTime.now().minusYears(1);
            
            var allUsers = userRepository.findAll();
            allUsers.forEach(user -> {
                long attendanceCount = coinTransactionRepository.countByUserIdAndReason(
                    user.getId(),
                    CoinReason.ATTENDANCE
                );

                if (attendanceCount >= 5) {
                    user.setWeenCoinBalance(user.getWeenCoinBalance() + 500);
                    userRepository.save(user);

                    CoinTransaction transaction = CoinTransaction.builder()
                        .userId(user.getId())
                        .amount(500)
                        .reason(CoinReason.ANNUAL_ACHIEVEMENT)
                        .relatedEntityId(null)
                        .build();
                    coinTransactionRepository.save(transaction);
                    log.info("Annual achievement award given to user: {} with {} attendances", 
                        user.getId(), attendanceCount);
                }
            });

            log.info("Annual achievement awards completed");
        } catch (Exception ex) {
            log.error("Failed to award annual achievements", ex);
        }
    }
}
