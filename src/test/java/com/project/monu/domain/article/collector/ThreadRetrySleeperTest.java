package com.project.monu.domain.article.collector;

import com.project.monu.domain.article.collector.exception.RetryInterruptedException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThreadRetrySleeperTest {

    private final ThreadRetrySleeper retrySleeper = new ThreadRetrySleeper();

    @Test
    void 재시도_대기중_인터럽트가_발생하면_수집_예외를_던지고_인터럽트_상태를_복원한다() {
        Thread.currentThread().interrupt();

        try {
            assertThatThrownBy(()-> retrySleeper.sleep(1000L))
                    .isInstanceOf(RetryInterruptedException.class)
                    .hasMessage("기사 수집 재시도 대기중 인터럽트가 발생했습니다.");

            assertThat(Thread.currentThread().isInterrupted())
                    .isTrue();
        } finally {
            // 다음 테스트에 인터럽트 상태가 전파되지 않도록 정리
            Thread.interrupted();
        }
    }

}