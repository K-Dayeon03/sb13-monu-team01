package com.project.monu.domain.interest.util;

public class InterestSimilarityCalculator {

    private static final double SIMILARITY_THRESHOLD = 0.8;

    // 이름이 짧을수록 글자 하나 차이가 비율(유사도)을 크게 흔들어서
    // 오탐(중복인데 못 잡거나, 반대로 중복 아닌데 걸림)이 잦다.
    // 이 길이 이하인 이름 쌍은 비율 대신 절대 편집 거리로 판단한다.
    private static final int SHORT_NAME_LENGTH_THRESHOLD = 4;
    private static final int SHORT_NAME_MAX_DISTANCE = 1;

    private InterestSimilarityCalculator() {
    }

    public static boolean isSimilar(String a, String b) {
        if (a.equals(b)) {
            return true;
        }

        int distance = levenshteinDistance(a, b);
        int maxLength = Math.max(a.length(), b.length());

        if (maxLength <= SHORT_NAME_LENGTH_THRESHOLD) {
            return distance <= SHORT_NAME_MAX_DISTANCE;
        }

        return calculate(distance, maxLength) >= SIMILARITY_THRESHOLD;
    }

    public static double calculate(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        int maxLength = Math.max(a.length(), b.length());
        if (maxLength == 0) {
            return 1.0;
        }
        int distance = levenshteinDistance(a, b);
        return calculate(distance, maxLength);
    }

    private static double calculate(int distance, int maxLength) {
        return 1.0 - ((double) distance / maxLength);
    }

    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + cost
                    );
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}
