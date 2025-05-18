package com.altester.core.util;

import com.altester.core.model.subject.Attempt;
import com.altester.core.repository.AttemptRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiAccuracy {

  private final AttemptRepository attemptRepository;

  /**
   * Calculates the AI grading accuracy percentage based on the match between teacher scores and AI
   * scores for all attempts where both scores exist.
   *
   * @return The accuracy percentage rounded to 1 decimal place
   */
  public double calculateAiAccuracy() {
    List<Attempt> attempts = attemptRepository.findAllWithBothScores();

    if (attempts.isEmpty()) {
      return 0.0;
    }

    int totalDiff = 0;
    int totalMaxPossibleDiff = 0;

    for (Attempt attempt : attempts) {
      int diff = Math.abs(attempt.getScore() - attempt.getAiScore());
      int maxPossibleDiff = attempt.getTest().getTotalScore();

      if (maxPossibleDiff <= 0) {
        continue;
      }

      totalDiff += diff;
      totalMaxPossibleDiff += maxPossibleDiff;
    }

    if (totalMaxPossibleDiff == 0) {
      return 0.0;
    }
    double accuracyPercentage = 100.0 - ((double) totalDiff / totalMaxPossibleDiff * 100.0);

    BigDecimal bd = BigDecimal.valueOf(accuracyPercentage);
    bd = bd.setScale(1, RoundingMode.HALF_UP);

    return bd.doubleValue();
  }
}
