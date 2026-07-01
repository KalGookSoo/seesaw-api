# JWT 인증 구현 가이드

## 개요

이 문서는 Spring Security와 JWT(JSON Web Token)를 사용한 인증 시스템의 구현에 대해 설명합니다. 이 애플리케이션은 무상태(Stateless) JWT 기반 인증을 사용하여 사용자를 인증하고 권한을 부여합니다.

## 주요 컴포넌트

### 1. JWT 토큰 제공자 (JwtTokenProvider)

`JwtTokenProvider`는 JWT 토큰의 생성, 검증 및 파싱을 담당합니다.

- **액세스 토큰**: 짧은 수명(1시간)을 가지며 API 요청 인증에 사용됩니다.
- **리프레시 토큰**: 긴 수명(14일)을 가지며 액세스 토큰이 만료되었을 때 새로운 액세스 토큰을 발급받는 데 사용됩니다.

```java
// 토큰 생성 예시
TokenInfo tokenInfo = jwtTokenProvider.generateTokenInfo(userPrincipal);
```

### 2. 인증 필터 (JwtAuthenticationFilter)

`JwtAuthenticationFilter`는 모든 HTTP 요청을 가로채서 JWT 토큰을 검증하고 인증 정보를 설정합니다.

- 요청 헤더에서 JWT 토큰을 추출합니다.
- 토큰이 유효하면 인증 정보를 `SecurityContext`에 설정합니다.
- 토큰이 없거나 유효하지 않으면 인증되지 않은 요청으로 처리합니다.

### 3. 헤더 기반 인증 제공자 (HeaderPrincipalProvider)

`HeaderPrincipalProvider`는 HTTP 요청 헤더에서 JWT 토큰을 추출하고 인증 객체를 제공합니다.

```java
// 인증 객체 가져오기
Authentication authentication = principalProvider.getAuthentication();
```

### 4. 보안 설정 (SecurityConfig)

`SecurityConfig`는 Spring Security 설정을 담당합니다.

- CSRF 보호 비활성화 (JWT 기반 인증에서는 일반적으로 필요하지 않음)
- CORS 설정
- 세션 관리 설정 (STATELESS)
- URL 기반 접근 제어 설정
- JWT 인증 필터 등록

## 인증 흐름

1. **로그인 (토큰 발급)**
   - 클라이언트가 사용자 자격 증명을 `/api/sign-in` 엔드포인트로 전송합니다.
   - 서버는 자격 증명을 검증하고 액세스 토큰과 리프레시 토큰을 발급합니다.

2. **API 요청 인증**
   - 클라이언트는 모든 API 요청의 `Authorization` 헤더에 `Bearer {액세스 토큰}`을 포함합니다.
   - `JwtAuthenticationFilter`가 토큰을 검증하고 인증 정보를 설정합니다.
   - 보호된 리소스에 대한 접근이 허용됩니다.

3. **토큰 갱신**
   - 액세스 토큰이 만료되면 클라이언트는 리프레시 토큰을 `/api/token/refresh` 엔드포인트로 전송합니다.
   - 서버는 리프레시 토큰을 검증하고 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.

## 메서드 수준 보안

Spring Security의 메서드 수준 보안을 사용하여 특정 메서드에 대한 접근을 제한할 수 있습니다.

```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() {
    // 관리자만 접근 가능한 메서드
}

@PreAuthorize("isAuthenticated()")
public void authenticatedOnlyMethod() {
    // 인증된 사용자만 접근 가능한 메서드
}

@PreAuthorize("hasAuthority('READ_SITE')")
public void authorizedOnlyMethod() {
    // 특정 권한을 가진 사용자만 접근 가능한 메서드
}
```

## 보안 고려사항

1. **토큰 저장**: 클라이언트는 토큰을 안전하게 저장해야 합니다. 브라우저 환경에서는 HttpOnly 쿠키나 로컬 스토리지를 사용할 수 있습니다.
2. **토큰 만료**: 액세스 토큰은 짧은 수명을 가지도록 설정하여 탈취 시 피해를 최소화합니다.
3. **HTTPS**: 모든 API 통신은 HTTPS를 통해 이루어져야 합니다.
4. **토큰 갱신**: 리프레시 토큰을 사용하여 액세스 토큰을 갱신하는 메커니즘을 구현합니다.

## 예제 코드

### 로그인 요청

```http
POST /api/sign-in
Content-Type: application/json

{
  "username": "user",
  "password": "password"
}
```

### 응답

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 3600000
}
```

### 인증된 API 요청

```http
GET /api/protected-resource
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 토큰 갱신 요청

```http
POST /api/token/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```