package com.project.monu.domain.article.collector.naver;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.naver.dto.NaverNewsResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NaverCollectorTest {

    @Mock
    private NaverNewsClient naverNewsClient;

    private NaverCollector naverCollector;

    @BeforeEach
    void setUp() {
        naverCollector = new NaverCollector(naverNewsClient, new NaverArticleMapper());
    }


    @Test
    @DisplayName("네이버에서 받은 응답을 CollectedArticle 리스트로 변환한다.")
    void 네이버_응답을_CollectedArticle_리스트로_변환한다() {
        // given
        NaverNewsResponse response = new NaverNewsResponse(
                2, 1, 2,
                List.of(
                        new NaverNewsResponse.Item(
                                "제목1", "https://example.com/1", "link1",
                                "요약1", "Thu, 13 Aug 2026 19:18:00 +0900"
                        ),
                        new NaverNewsResponse.Item(
                                "제목2", "https://example.com/2", "link2",
                                "요약2", "Thu, 13 Aug 2026 19:20:00 +0900"
                        )
                )
        );
        when(naverNewsClient.search("반도체")).thenReturn(response);

        // when
        List<CollectedArticle> result = naverCollector.collect("반도체");

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("제목1");
        assertThat(result.get(1).originalLink()).isEqualTo("https://example.com/2");
    }

}
