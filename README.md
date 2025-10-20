# 🚗 CarFit - 스마트 자동차 관리 플랫폼

> **실시간 유가 정보, 위치 기반 서비스, 커뮤니티를 통한 종합적인 자동차 관리 솔루션**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [테스트](#-테스트)
- [모니터링](#-모니터링)


## 🎯 프로젝트 개요

CarFit은 자동차 소유자들을 위한 종합적인 관리 플랫폼입니다. 실시간 유가 정보, 위치 기반 주유소 검색, 커뮤니티 기능을 통해 사용자들이 더 스마트하게 자동차를 관리할 수 있도록 돕습니다.

### 🌟 핵심 가치
- **실시간 데이터**: Opinet API를 통한 최신 유가 정보 제공
- **정확한 위치 서비스**: Naver Map API와 좌표 변환을 통한 정밀한 위치 기반 서비스
- **보안 중심**: JWT + Redis 기반의 강력한 인증 시스템
- **확장 가능**: 마이크로서비스 아키텍처 기반의 모듈화된 설계



## 🚀 주요 기능

### 🔐 사용자 인증 및 보안
- **JWT 기반 인증**: Access Token (30분) + Refresh Token (7일)
- **Redis 토큰 관리**: 블랙리스트를 통한 보안 강화
- **이메일 인증**: 회원가입 시 이메일 인증 시스템
- **프로필 관리**: AWS S3 기반 프로필 이미지 업로드
- **데이터 암호화**: AES 암호화를 통한 개인정보 보호

### ⛽ 유가 정보 관리

- **실시간 유가 조회**: Opinet API 연동
- **지역별 유가 분석**: 시도/시군구별 평균 유가 제공
- **유가 데이터 저장**: MySQL 기반 데이터 영속화
- **스케줄링**: 정기적인 유가 데이터 업데이트

### 🗺️ 위치 및 지도 서비스
- **지오코딩**: 주소를 좌표로 변환 (Naver Map API)
- **좌표 변환**: WGS84 ↔ TM128 정밀 변환 (1cm 정확도)
- **주유소 검색**: 위치 기반 주변 주유소 검색
- **사용자 위치 관리**: 기본 위치 설정 및 관리

### 🏪 주유소 정보
- **주변 주유소 검색**: 현재 위치 기반 주유소 찾기
- **상세 정보 제공**: 주유소별 상세 정보 및 리뷰
- **실시간 가격**: 각 주유소별 실시간 유가 정보

### 💬 커뮤니티
- **게시판**: 자동차 관련 정보 공유
- **댓글 시스템**: 게시글별 댓글 작성 및 관리
- **사용자 인증**: 로그인한 사용자만 게시글 작성 가능

### 🛡️ 보험 계산
- **자동차 보험료 계산**: 차량 정보 기반 보험료 산출
- **실시간 계산**: 즉시 보험료 견적 제공

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.4.4
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis 7.0
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **Build Tool**: Gradle
- **Monitoring**: Spring Actuator + Prometheus

### External APIs
- **Opinet API**: 실시간 유가 정보
- **Naver Map API**: 지도 및 지오코딩 서비스
- **AWS S3**: 파일 저장소

### Development Tools
- **Testing**: JUnit 5, Mockito
- **Code Quality**: JaCoCo (코드 커버리지)
- **Documentation**: JavaDoc
- **Version Control**: Git + GitHub

## 📁 프로젝트 구조

```
backend/
├── src/main/java/backend/
│   ├── auth/                    # 인증 및 보안
│   │   ├── controller/         # AuthController
│   │   ├── domain/             # JWT 관련 엔티티
│   │   ├── dto/                # 인증 요청/응답 DTO
│   │   ├── repository/         # 사용자 인증 저장소
│   │   ├── security/           # JWT 필터, 토큰 관리
│   │   └── service/            # 인증 서비스
│   ├── common/                 # 공통 기능
│   │   ├── config/             # 설정 클래스
│   │   ├── exception/          # 전역 예외 처리
│   │   ├── scheduler/          # 스케줄러 설정
│   │   └── util/               # 유틸리티 클래스
│   ├── community/              # 커뮤니티 기능
│   │   ├── controller/         # CommunityController
│   │   ├── domain/             # Post, Comment 엔티티
│   │   ├── dto/                # 커뮤니티 DTO
│   │   ├── repository/         # 게시글, 댓글 저장소
│   │   └── service/            # 커뮤니티 서비스
│   ├── geolocation/            # 지오코딩 서비스
│   │   └── controller/         # GeoLocationController
│   ├── insurance/              # 보험 계산
│   │   ├── controller/         # InsuranceController
│   │   ├── dto/                # 보험 관련 DTO
│   │   └── service/            # 보험 계산 서비스
│   ├── location/               # 위치 관리
│   │   ├── controller/         # UserDefaultLocationController
│   │   ├── domain/             # 위치 관련 엔티티
│   │   ├── dto/                # 위치 DTO
│   │   ├── repository/         # 위치 저장소
│   │   └── service/            # 위치 서비스
│   ├── oilprice/               # 유가 정보
│   │   ├── controller/         # OilPriceController
│   │   ├── domain/             # 유가 엔티티
│   │   ├── dto/                # 유가 DTO
│   │   ├── repository/         # 유가 저장소
│   │   └── service/            # 유가 서비스
│   ├── station/                # 주유소 정보
│   │   ├── controller/         # StationController
│   │   └── service/            # 주유소 서비스
│   └── user/                   # 사용자 관리
│       ├── controller/         # UserController, ProfileController
│       ├── domain/             # User 엔티티
│       ├── dto/                # 사용자 DTO
│       ├── repository/         # 사용자 저장소
│       └── service/            # 사용자 서비스
├── src/main/resources/
│   ├── application.properties  # 애플리케이션 설정
│   └── static/                 # 정적 리소스
└── src/test/                   # 테스트 코드
```




## 🧪 테스트

### 단위 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test




## 📊 모니터링

### Spring Actuator
- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

### 주요 모니터링 지표
- JVM 메모리 사용량
- 데이터베이스 연결 풀 상태
- Redis 연결 상태
- API 응답 시간
- 에러 발생률

### 로깅
- **로그 레벨**: DEBUG (개발), INFO (운영)
- **로그 파일**: `logs/application.log`
- **구조화된 로깅**: JSON 형태로 로그 출력


### 코딩 컨벤션
- Java: Google Java Style Guide
- 커밋 메시지: Conventional Commits
- PR 템플릿: `.github/PULL_REQUEST_TEMPLATE.md` 사용




---

<div align="center">


Made with ❤️ by CarFit 

</div>