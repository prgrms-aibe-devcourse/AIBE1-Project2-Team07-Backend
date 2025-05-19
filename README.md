# 🐾 PetTalk - 반려생활, 같이 고민해요

PetTalk은 반려인들의 고민을 해결해주는 종합 플랫폼입니다.  
믿을 수 있는 훈련사 매칭부터 커뮤니티 활동까지, 반려동물과의 더 행복한 생활을 도와드립니다.

## 👥 팀 소개

|                         김주엽                          |                          권규태                           |                           한정호                           |                          진소희                           |                           조준호                           |
|:----------------------------------------------------:|:------------------------------------------------------:|:-------------------------------------------------------:|:------------------------------------------------------:|:-------------------------------------------------------:|
| ![김주엽](https://avatars.githubusercontent.com/kjyy08) | ![권규태](https://avatars.githubusercontent.com/rbxo0128) | ![한정호](https://avatars.githubusercontent.com/hanjungho) | ![진소희](https://avatars.githubusercontent.com/soheeGit) | ![조준호](https://avatars.githubusercontent.com/lSNOTNULL) |
|         [@kjyy08](https://github.com/kjyy08)         |        [@rbxo0128](https://github.com/rbxo0128)        |       [@hanjungho](https://github.com/hanjungho)        |        [@soheeGit](https://github.com/soheeGit)        |       [@lSNOTNULL](https://github.com/lSNOTNULL)        |

---

## 💡 프로젝트 개요

### 문제 인식

- 반려동물을 키우는 가구가 증가하면서 행동 교정, 훈련 등에 대한 전문적인 도움 필요성 증가
- 신뢰할 수 있는 전문 훈련사를 찾기 어려운 현실
- 반려동물 관련 정보와 경험을 공유할 수 있는 커뮤니티 부족

### 해결책

- 검증된 훈련사 프로필과 리뷰 시스템을 통한 신뢰성 있는 매칭 서비스
- AI 기반 챗봇으로 간단한 질문에 즉시 답변
- 다양한 주제별 커뮤니티로 반려인들 간의 소통 강화

## 🔍 주요 기능

### 훈련사 매칭

- 지역, 전문 분야, 서비스 유형 등 다양한 필터로 맞춤형 훈련사 검색
- 훈련사 프로필, 자격증, 경력 등 상세 정보 제공
- 방문 교육과 영상 교육 옵션 제공
- 상담 신청 및 관리 시스템

### AI 챗봇 상담

- 반려동물 행동, 건강 관련 빠른 정보 제공
- 훈련사 추천 및 게시글 검색 기능
- MCP(Model Context Protocol) 활용으로 다양한 질의응답 지원

### 커뮤니티

- 자유게시판, 펫 도구 후기, 질문 게시판, 자랑하기 등 다양한 게시판
- 이미지, 동영상 첨부 기능
- 태그 시스템과 AI 추천 태그 기능
- 댓글과 좋아요 기능으로 활발한 소통

### 사용자 계정 관리

- 카카오, 네이버 소셜 로그인 지원
- 사용자/훈련사 역할 분리
- 마이페이지에서 활동 내역 관리

### 관리자 기능

- 훈련사 자격증 인증 시스템
- 사용자 관리 및 게시글/댓글 관리
- 리뷰 관리 시스템

## 🛠️ 기술 스택

### 백엔드

- **언어 및 프레임워크**: Java 17, Spring Boot
- **보안**: Spring Security, JWT, OAuth2.0
- **데이터베이스**: MySQL, JPA/Hibernate
- **API 문서화**: Swagger/OpenAPI
- **빌드 도구**: Gradle

### 프론트엔드

- **기본 기술**: HTML, CSS, JavaScript
- **프레임워크 및 라이브러리**: Bootstrap, Express.js

### DevOps

- **클라우드**: AWS Lightsail
- **파일 스토리지**: AWS S3
- **컨테이너화**: Docker
- **CI/CD**: GitHub Actions\(CI), Jenkins\(CD)

### AI

- **프레임워크**: LangChain4j, Spring AI
- **모델**, Google Gemini 2.0 Flash

## 📚 API 문서

API 문서는 Swagger UI를 통해 제공됩니다. 애플리케이션이 실행된 후 다음 URL에서 확인할 수 있습니다:

- 개발 환경: `http://localhost:8080/swagger-ui/index.html`
- 개발 서버: `https://dev-api.pettalk.example.com/swagger-ui/index.html`
- 운영 서버: `https://api.pettalk.example.com/swagger-ui/index.html`

API 문서에서는 다음과 같은 정보를 확인할 수 있습니다:

- 모든 API 엔드포인트 목록
- 요청/응답 형식 및 예시
- 인증 방법 (JWT Bearer 토큰)
- API 테스트 기능

## 🚀 배포 가이드

### 로컬 개발 환경 설정

1. 저장소 클론
   ```bash
   git clone https://github.com/your-organization/pettalk-backend.git
   cd pettalk-backend
   ```

2. 애플리케이션 실행
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

### Docker를 이용한 배포

1. Docker 이미지 빌드
   ```bash
   docker build -t pettalk-backend:latest -f docker/Dockerfile .
   ```

2. Docker 컨테이너 실행
   ```bash
   docker run -d \
     --name pet-talk \
     -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=dev \
     pettalk-backend:latest
   ```

### CI/CD 파이프라인

프로젝트는 Jenkins를 사용한 CI/CD 파이프라인이 구성되어 있습니다:

1. GitHub 저장소에 코드 푸시
2. Docker 이미지 빌드 및 GitHub Container Registry(ghcr.io)에 푸시
3. Jenkins 파이프라인 자동 트리거
4. 브랜치에 따른 환경 설정:
    - `main` 브랜치: 운영 환경 (포트: 8443, 컨테이너: pet-talk-main)
    - `dev` 브랜치: 개발 환경 (포트: 8444, 컨테이너: pet-talk-dev)
5. 서버에서 이미지 풀 및 컨테이너 실행

## 🚀 미래 계획

- API 응답 속도 개선
- AI 챗봇 성능 개선 및 특화 기능 추가
