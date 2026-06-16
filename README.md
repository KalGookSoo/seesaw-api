# ⚖️ SEESAW API

> **SEESAW 프로젝트의 핵심 비즈니스 로직을 처리하는 Headless REST API 서버**

SEESAW API는 관리자 콘솔(`seesaw-console`) 및 웹 프론트엔드(`seesaw-web`)에서 공통으로 참조하는 핵심 백엔드 모듈입니다. API-First 디자인 원칙을 준수하며, 유연한 콘텐츠 관리와 강력한 보안 기능을 제공합니다.

---

## ✨ Key Features

### 1. Headless CMS Architecture
- 모든 기능을 RESTful API로 노출하여 다양한 클라이언트(Web, Mobile, Console)에서 활용 가능
- 카테고리, 게시글, 댓글, 사이트 설정 등 콘텐츠 전반에 대한 통합 관리

### 2. Advanced Attachment System
- **다양한 미리보기 지원**: 이미지, PDF는 물론 HTML, CSS, JS, JSON, XML, TXT 등 텍스트/코드 파일의 인라인 미리보기 제공
- **보안 미리보기**: HTML 및 스크립트 파일 미리보기 시 CSP(Content-Security-Policy) sandbox를 적용하여 악성 스크립트 실행 차단
- **MIME 타입 기반 UI**: 파일 형식에 최적화된 동적 아이콘 및 메타데이터 제공

### 3. Robust Security & Permission
- **JWT 기반 인증**: Stateless한 인증 체계를 통한 확장성 확보
- **Context 기반 권한 검증**: 도메인별 Context 계층을 도입하여 비즈니스 로직과 복잡한 권한 검증(ACL)을 분리
- **Spring Security 연동**: 메서드 레벨(`@PreAuthorize`) 및 URL 레벨의 세밀한 접근 제어

### 4. Enterprise-Ready Configuration
- **Spring Cloud Config**: 외부 설정 서버와의 연동을 통해 무중단 설정 변경 및 환경별(Dev, Prod) 관리 최적화
- **Multi-Module Structure**: `seesaw-core`와의 결합을 통해 코드 재사용성 및 모듈화 극대화

---

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.4.x
- **Language**: Java 17
- **Build Tool**: Gradle
- **Persistence**: Spring Data JPA (Hibernate)
- **Security**: Spring Security, JWT
- **Configuration**: Spring Cloud Config Client
- **Documentation**: OpenAPI 3.0 (Swagger UI)

---

## 🏗 Architecture

SEESAW API는 다음과 같은 계층화된 아키텍처를 따릅니다.

1.  **Controller Layer**: 요청 수신 및 응답 반환. `@Valid`를 통한 입력 검증 및 권한 선언.
2.  **Context/Service Layer**: 핵심 비즈니스 로직 수행 및 도메인 간 조율.
3.  **Repository Layer**: 데이터베이스 접근 및 영속성 관리.
4.  **Model/Command**: 불변 객체(Model)를 통한 데이터 출력 및 커맨드 객체(Command)를 통한 데이터 입력 분리.

---

## 🚀 Quick Start

### Prerequisites
- JDK 17 이상
- Gradle 8.x 이상
- Running Spring Cloud Config Server (Optional, but recommended)

### Running the Application
```bash
./gradlew :seesaw-api:bootRun
```

### API Documentation
애플리케이션 실행 후 아래 주소에서 Swagger UI를 통해 API 명세를 확인할 수 있습니다.
- `http://localhost:8080/swagger-ui.html`

---

## 📂 Project Structure
```text
seesaw-api/
├── src/main/java/kr/me/seesaw/
│   ├── controller/      # REST API 컨트롤러
│   ├── context/         # 비즈니스 로직 및 권한 검증 컨텍스트
│   ├── security/        # JWT 및 보안 설정
│   └── service/         # 서비스 인터페이스 및 구현체
└── src/main/resources/
    ├── messages.properties  # 다국어 메시지 설정
    └── application.yml      # 애플리케이션 설정
```
