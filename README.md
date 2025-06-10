# 팀 프로젝트 및 업무 관리 시스템

### 기술 스택
- **Language**: Java 17
- **Framework**: Spring Boot
- **Batch Framework**: Spring Batch
- **Database**: MySQL
- **ORM**: JPA + QueryDSL
- **Authentication**: Spring Security + JWT
- **Documentation**: Swagger
- **Logging**: AOP + ELK Stack
- **Testing**: JUnit 5, Testcontainers
- **Build Tool**: Gradle

### 주요 기능
- **회원(Member)** - 생성, 조회, 수정, 삭제
- **프로젝트(Project)** - 생성, 조회, 수정, 삭제
- **프로젝트 회원(ProjectMember)** - 생성, 수정, 삭제
- **업무(Task)** - 생성, 조회, 수정, 삭제
- **알림** - 조회, 읽음 처리, 삭제
- **배치 작업** - 취소 상태인 오래된 업무 및 확인된 오래된 알림 삭제, 당일 미완료된 업무 알림 생성
- **로그 모니터링** - AOP + ELK를 활용한 로그 수집 및 모니터링