# 🚀 GpsOptimizer

**GpsOptimizer**는 지저분한 GPS 데이터를 정교하게 정제하고 최적화하기 위한 고성능 Java 라이브러리입니다. 러닝, 사이클링, 드라이빙 등 움직임이 포함된 모든 트래킹 데이터에 최적화되어 있습니다.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://img.shields.io/badge/Performance-100k_pts_/_23ms-brightgreen.svg)]()

## ✨ 주요 기능

- **지능형 이상치 제거 (Outlier Removal)**: Moving Median Window 알고리즘을 사용하여 속도나 거리 기준으로 튀는 좌표를 안정적으로 제거합니다.
- **칼만 필터 평활화 (Kalman Smoothing)**: 1차 속도 모델(1st-order Velocity Model)을 적용하여 경로를 부드럽게 보정하며, 시작점과 끝점을 정확하게 보존합니다.
- **경로 단순화 (Path Simplification)**: Douglas-Peucker 알고리즘을 통해 경로의 특징을 유지하면서 불필요한 좌표를 획기적으로 줄여 데이터 용량을 최적화합니다.
- **압도적인 성능 (High Performance)**: 최적화된 내부 연산 및 Primitive 타입 활용으로 **10만 개의 좌표를 약 23ms** 만에 처리합니다.
- **Fluent API**: 직관적인 빌더 패턴 스타일의 API를 제공하여 코드 가독성을 높였습니다.

## 🛠 사용 예시

가독성 높은 Fluent API를 통해 단 몇 줄의 코드로 복잡한 정제 파이프라인을 구축할 수 있습니다.

```java
import core.*;
import infrastructure.*;

// 1. 원본 트랙 생성
GpsTrack rawTrack = ...;

// 2. 정제 파이프라인 실행
CleaningResult result = TrackCleaner.of(rawTrack)
    .removeOutliers(Threshold.ofKmPerHour(30))  // 시속 30km 이상 튀는 좌표 제거
    .removeOutliers(Threshold.ofMeters(100))    // 중심에서 100m 이상 벗어난 좌표 제거
    .smooth(Algorithm.KALMAN)                   // 칼만 필터로 경로 부드럽게 보정
    .simplify(Tolerance.ofMeters(1))            // 1m 오차 범위 내 직선 구간 단순화
    .clean();

// 3. 결과 확인
GpsTrack cleanedTrack = result.cleanedTrack();
result.stepReports().forEach(report -> {
    System.out.printf("[%s] %d -> %d (제거/보정: %d)%n", 
        report.filterName(), report.beforeCount(), report.afterCount(), report.modifiedCount());
});
```

## 📊 성능 지표 (JMH Benchmark)

JMH(Java Microbenchmark Harness)를 통해 측정된 10만 개 좌표 처리 성능 결과입니다.

| 단계 | 처리 속도 (Average Time) |
| :--- | :--- |
| **전체 파이프라인 실행** | **~23.03 ms / 100,000 pts** |

*측정 환경: JDK 21, Apple M1*

## 🔍 기술적 특징

- **Robust Median Search**: 중앙값 계산 시 타겟 좌표를 포함하여 이상치가 적은 윈도우에서 정상 데이터를 보호하는 안정성을 확보했습니다.
- **1st-order Kalman Model**: 정지 상태 가정이 아닌 속도 변화량을 추적하는 모델을 사용하여 실제 이동 궤적을 정확하게 복원합니다.

## 📄 라이선스

이 프로젝트는 **MIT License**를 따릅니다. 자세한 내용은 [LICENSE](./LICENSE) 파일을 참조하세요.
