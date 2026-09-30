# integration-api-demo

Java / Spring Boot로 외부 도서 검색 API를 연동하고, 가공한 도서 정보를 PostgreSQL에 즐겨찾기로 저장하는 백엔드 포트폴리오 프로젝트입니다.

## 현재 진행 상태

2단계: 카카오 도서 검색 및 ISBN 상세 조회, 응답 DTO 변환, 입력 검증과 외부 API 오류 처리를 구현했습니다.
즐겨찾기, PostgreSQL, 인증 기능은 아직 구현하지 않았습니다.

## 기술 구성

- Java 25 / Spring Boot 4.1.1
- Gradle Wrapper / Spring MVC / Bean Validation
- springdoc-openapi 3.1.1 (Swagger UI)
- RestClient (연결 제한 3초, 읽기 제한 5초)
- 후속 단계: PostgreSQL, Spring Data JPA

## Windows PowerShell 실행

```powershell
# demo 저장소의 루트에서 실행합니다.
cd integration-api-demo
.\gradlew.bat test
# 실제 키로 교체하여 같은 터미널에서 설정합니다. 키는 저장소에 기록하지 않습니다.
$env:KAKAO_REST_API_KEY="본인의 REST API 키"
.\gradlew.bat bootRun
```

Gradle 별도 설치는 필요하지 않습니다. 최초 실행은 Gradle과 라이브러리를 다운로드하므로 인터넷 연결이 필요합니다. JDK 25를 사용합니다. `JAVA_HOME`은 JDK 25 설치 폴더로 설정합니다. Gradle Java Toolchain도 25로 지정하여 컴파일·테스트·애플리케이션 실행에 같은 버전을 사용합니다.

- Swagger: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- 실행 확인: http://localhost:8080/api/status

현재는 로컬 PC에서만 접속하도록 설정했습니다. 실행 확인 API는 DB 및 외부 API의 정상 동작을 의미하지 않습니다. 서버 종료는 실행한 터미널에서 Ctrl+C입니다.

## Java 25 전환 검증 결과

- 설치된 JDK: Oracle JDK 25.0.2
- Java Toolchain 25로 `gradlew.bat test bootJar` 성공 (Spring 컨텍스트 테스트 및 실행 JAR 생성)
- 2단계에서는 Java 25 실행 JAR를 임시 포트 18080에서 구동해 Swagger HTTP 200, 도서 API 문서 생성, 잘못된 입력 400, 키 미설정 503을 확인했습니다. 검증 후 임시 서버를 종료했습니다.

## 1단계 HTTP 검증 기록 (Java 21)

- 서버 실행 후 `/api/status`: `status=UP` 확인
- `/v3/api-docs`: 문서 제목 및 `/api/status` 경로 확인
- `/swagger-ui/index.html`: HTTP 200 확인 (브라우저 화면의 시각 검증은 아직 수행하지 않음)

## 구현 순서

1. 기본 프로젝트와 Swagger 실행 확인
2. 카카오 도서 검색 연동, 검색 결과 DTO 변환, ISBN 상세 조회
3. PostgreSQL 연결, JPA 즐겨찾기 등록/목록/삭제
4. 입력 검증, 공통 예외 응답, 외부 API 타임아웃, 중복 저장 처리, 핵심 테스트
5. API 예제, 실제 검증 결과, Swagger 캡처 및 포트폴리오 설명 정리
6. 선택적 2차 작업: Spring Security + JWT, 사용자별 즐겨찾기

## 설계 방향

기능별 패키지(book, favorite) 안에 Controller, Service, DTO를 배치합니다. 외부 HTTP 호출은 integration.kakao 패키지로 분리하고, 공통 설정과 예외 처리만 공유합니다. 단일 모듈이며 불필요한 서비스 인터페이스나 범용 CRUD 추상화는 만들지 않습니다.

예정 흐름:

```text
키워드 검색 → 카카오 API → 응답 정규화 → 검색 결과 반환
즐겨찾기 등록(ISBN) → 카카오 API 조회 → 도서 정보 정규화 → DB 저장 → REST 응답
즐겨찾기 목록 → DB 조회 → REST 응답
```

카카오는 별도 도서 상세 API 대신 ISBN 검색을 제공합니다. ISBN 검색 결과 최대 50건에서 정확히 일치하는 ISBN을 대조하여 상세 조회합니다. 반환 ISBN은 ISBN13을 우선하고 없으면 ISBN10을 사용합니다. ISBN이 없는 검색 결과는 null을 반환합니다. ISBN 입력은 하이픈 없는 10자리(마지막 X 허용) 또는 숫자 13자리이며 체크섬은 검증하지 않습니다. 추후 즐겨찾기 정보는 저장 시점의 스냅샷으로 관리하며 자동 동기화는 범위에 포함하지 않습니다.

인증 전에는 하나의 공용 즐겨찾기 목록을 사용하는 로컬 데모입니다. 개인별 소유권은 JWT 단계에서 추가합니다. API 키와 DB 비밀번호는 환경변수로 관리하며 저장소에 기록하지 않습니다.

## API 구현 현황

| 메서드 | 경로 | 기능 |
|---|---|---|
| GET | /api/books/search?keyword=spring | 도서 검색 (구현) |
| GET | /api/books/{isbn} | ISBN 상세 조회 (구현) |
| POST | /api/favorites | ISBN으로 도서 정보 조회 후 저장 (예정) |
| GET | /api/favorites | 저장한 도서 목록 (예정) |
| DELETE | /api/favorites/{id} | 즐겨찾기 삭제 (예정) |

## 검색 시연 순서

1. 키를 설정한 PowerShell 터미널에서 서버를 실행합니다. 기존 서버가 있다면 해당 터미널에서 Ctrl+C로 종료한 후 재실행합니다. 환경변수는 이미 실행 중인 서버나 다른 터미널에 자동 전달되지 않습니다.
2. Swagger의 `01. 도서 검색`에서 검색 API를 열고 `Try it out`을 선택합니다.
3. keyword=`spring`, page=`1`, size=`10`으로 실행합니다. 검색어는 필수이며 최대 100자, page와 size는 1~50입니다.
4. 응답의 `books`에서 ISBN을 복사해 상세 조회에 입력합니다.
5. 빈 검색어, page=0, 잘못된 ISBN으로 400 오류 응답도 확인합니다.

응답은 `keyword`, `page`, `size`, `totalCount`, `end`, `books`로 구성됩니다. `books`에는 ISBN, 제목, 소개, 저자, 출판사, 표지 URL, 도서 URL을 포함합니다. 검색 결과는 DB에 저장하지 않습니다.

## 오류 응답

오류는 `code`, `message`, `timestamp` 형태로 반환합니다. 외부 서버의 오류 본문 및 인증 키를 반환하지 않습니다.

| HTTP | code | 의미 |
|---|---|---|
| 400 | INVALID_REQUEST | 검색 조건 또는 ISBN 형식 오류 |
| 404 | BOOK_NOT_FOUND | ISBN에 정확히 일치하는 도서 없음 |
| 502 | BOOK_API_ERROR | 외부 통신 오류, 인증 실패 또는 잘못된 응답 |
| 503 | BOOK_API_NOT_CONFIGURED | 실행 프로세스에 키 미설정 |
| 503 | BOOK_API_RATE_LIMITED | 카카오 요청 한도 도달 |
| 504 | BOOK_API_TIMEOUT | 외부 API 연결/읽기 시간 초과 |

## 코드 읽는 순서

- `book/BookController.java`: HTTP 파라미터 검증 및 Swagger 설명
- `book/BookService.java`: 검색어 정리, ISBN 정확 대조, 서비스 응답 변환
- `integration/kakao/KakaoBookClient.java`: 인증 헤더, 외부 HTTP 호출, 통신 오류 변환
- `integration/kakao/KakaoBookResponse.java`: 카카오의 JSON 형태를 받는 외부 DTO
- `book/BookResponse.java`: 클라이언트에게 제공할 DTO
- `common/ApiExceptionHandler.java`: HTTP 상태와 일관된 오류 응답

Controller → Service → Client로 요청하고, 외부 DTO를 Service에서 공개 DTO로 변환합니다. 카카오 응답 구조와 공개 API 구조를 분리해 외부 변경의 영향을 줄입니다.

## 2단계 검증 범위

자동 테스트는 실제 키 없이 모의 HTTP 응답을 사용합니다. 검색어 인코딩, 인증 헤더, 외부 JSON 변환, ISBN 우선순위와 정확 대조, 입력 검증, 키 미설정, 외부 오류·한도·타임아웃을 검사합니다. 실제 카카오 API 성공 호출은 사용자 터미널의 키로 별도 확인해야 합니다.

`gradlew.bat test bootJar` 성공: 기본 컨텍스트 테스트를 포함한 12개 테스트 통과. Swagger HTML HTTP 응답과 OpenAPI 경로는 검증했으며 브라우저 화면의 시각 검증은 아직 수행하지 않았습니다.

## 공식 참고 문서

- [Spring Initializr](https://start.spring.io/)
- [카카오 도서 검색 API](https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-book)
- [springdoc-openapi](https://springdoc.org/)
