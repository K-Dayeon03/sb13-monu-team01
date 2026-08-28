package com.project.monu.domain.article.collector;

import com.project.monu.domain.article.collector.exception.RetryInterruptedException;
import org.springframework.stereotype.Component;

@Component
public class ThreadRetrySleeper implements RetrySleeper {

    @Override
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RetryInterruptedException(
                    "기사 수집 재시도 대기중 인터럽트가 발생했습니다.",
                    e
            );
        }
    }
}
