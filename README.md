# Backend Demo Projects

Java / Spring Boot 기반 백엔드 개발 포트폴리오 모음입니다. 각 하위 폴더는 독립적으로 빌드하고 실행하는 프로젝트입니다.

| 프로젝트 | 주요 기능 | 진행 상태 |
|---|---|---|
| [integration-api-demo](integration-api-demo/README.md) | 카카오 도서 검색 API 연동, ISBN 상세 조회, 응답 가공, 입력 검증, Swagger | 검색·상세 조회 구현, PostgreSQL 즐겨찾기 예정 |

## 실행하기

JDK 25를 준비한 뒤 원하는 프로젝트 폴더에서 Gradle Wrapper를 실행합니다. API 키 설정 및 상세 실행 방법은 각 프로젝트 README를 참고하세요.

```powershell
cd integration-api-demo
.\gradlew.bat test
.\gradlew.bat bootRun
```

외부 API 키와 로컬 환경 설정은 저장소에 포함하지 않습니다.
