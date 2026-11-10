# PetClinic WAS (REST API)

AWS 3-Tier(WEB · WAS · DB) 인프라 구축 프로젝트의 **WAS 계층** 애플리케이션입니다.
[spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic)을 기반으로, 화면(Thymeleaf)을 제거하고 **JSON REST API만 제공**하도록 바꿨습니다. 보호자·반려동물·진료 기록·수의사에 대한 업무 로직은 원본 PetClinic을 그대로 사용합니다.

## 구성에서의 역할

```text
브라우저 ──▶ 외부 ALB ──▶ WEB (Apache 2.4 + React 빌드 결과)
                               │  /api/* 만 전달 (mod_proxy_http)
                               ▼
                           내부 ALB ──▶ WAS (이 애플리케이션, 포트 8080) ──▶ DB (RDS for MySQL)
```

- 화면은 WEB(React)이 담당하고, 이 애플리케이션은 `/api/**` 요청만 처리합니다.
- 내부 ALB 헬스 체크 경로는 `/actuator/health`입니다.

### WEB-WAS 연동 설정

연결을 재사용하는 쪽(앞단)이 먼저 끊도록 유휴 시간을 계단식으로 맞춥니다. 어긋나면 이미 닫힌 연결로 요청을 보내 간헐적으로 502가 납니다.

| 구간 | 설정 | 값 |
| --- | --- | --- |
| Apache → 내부 ALB | `ProxyPass /api http://<내부 ALB DNS>/api ttl=50` + `ProxyPassReverse` | 50초 |
| 내부 ALB | 유휴 타임아웃 (기본값) | 60초 |
| 내부 ALB → WAS | `server.tomcat.keep-alive-timeout` (`application.properties`) | 65초 |

| 항목 | 값 |
| --- | --- |
| 대상 그룹 | HTTP 8080, 헬스 체크 `/actuator/health` (성공 코드 200) |
| 등록 취소 지연 | 30~60초 권장 |
| WAS 종료 | `server.shutdown=graceful`, 처리 중인 요청을 최대 20초 기다린 뒤 종료 (등록 취소 지연보다 짧게) |

## 기술 스택

| 항목 | 버전 |
| --- | --- |
| Java | 21 (Amazon Corretto 21) |
| Spring Boot | 4.1.1 |
| 웹 서버 | 내장 Tomcat 11 (실행 가능한 JAR) |
| 영속성 | Spring Data JPA (Hibernate) |
| DB | H2(기본, 개발용) / MySQL(`mysql` 프로파일) |
| 빌드 | Gradle Wrapper(`gradlew`, Gradle 8.14.2) |

## 패키지 구조

계층별로 나눴습니다. 요청은 `controller → service → repository` 순서로 흐릅니다.

```text
org.springframework.samples.petclinic
├── controller   REST 컨트롤러 (보호자·반려동물·병원·예약 4개, 요청·응답 변환만)
├── service      업무 로직과 DB 조회가 필요한 검증 (보호자·반려동물·병원·예약 4개)
├── repository   Spring Data JPA 리포지토리
├── entity       JPA 엔티티 = 테이블 하나에 클래스 하나 (Owner, Pet, PetType, Visit, Hospital, Vet, Specialty, Appointment)
├── dto          요청·응답 JSON (OwnerDto, PetDto, HospitalDto, AppointmentDto 안에 Request·Response 레코드, PageResponse)
└── exception    예외와 오류 응답 처리 (RFC 9457)
```

엔티티는 응답으로 바로 내보내지 않고 DTO로 바꿔서 보냅니다(지연 로딩 프록시·순환 참조 때문). 입력 검증은 DTO의 Bean Validation(`@NotBlank`, `@Pattern` 등)이 하고, DB를 봐야 하는 검증(없는 ID, 예약 시간 중복)만 서비스가 합니다. 보호자나 반려동물을 지울 때는 진료 예약을 먼저 지우고, 이어서 JPA cascade가 진료 기록 → 반려동물 → 보호자 순서로 지웁니다.

## API

전체 명세는 [docs/api.md](docs/api.md)에 있습니다.

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| GET | `/api/owners?name=&page=&size=` | 보호자 검색 (이름으로 시작) |
| GET · POST · PUT · DELETE | `/api/owners`, `/api/owners/{ownerId}` | 보호자 상세·등록·수정·삭제 |
| GET · POST · PUT · DELETE | `/api/owners/{ownerId}/pets`, `/api/owners/{ownerId}/pets/{petId}` | 반려동물 상세·등록·수정·삭제 |
| GET · POST · PUT · DELETE | `/api/owners/{ownerId}/pets/{petId}/visits`, `…/visits/{visitId}` | 진료 기록 목록·등록·수정·삭제 |
| GET | `/api/hospitals`, `/api/hospitals/{hospitalId}`, `/api/hospitals/{hospitalId}/vets` | 병원 목록·상세·소속 수의사 |
| GET | `/api/vets?page=&size=` | 수의사 목록 (병원 포함, 페이지 단위) |
| GET · POST · PUT · PATCH · DELETE | `/api/appointments`, `/api/appointments/{appointmentId}`, `…/status` | 진료 예약 목록·상세·등록·변경·상태 변경·삭제 |
| GET | `/api/pettypes` | 동물 종류 목록 |
| GET | `/actuator/health` | 헬스 체크 |

오류는 RFC 9457 Problem Details 형식으로 응답하고, 검증 오류 메시지는 요청의 `Accept-Language`(예: `ko`)를 따릅니다.

## 로컬에서 실행

JDK 21이 필요합니다.

```bash
./gradlew bootRun
```

<http://localhost:8080/api/owners> 또는 <http://localhost:8080/actuator/health>로 확인합니다. 기본 DB는 H2(메모리)입니다. 테이블은 따로 SQL 파일 없이 **Hibernate가 엔티티 클래스를 보고 기동할 때 만들고**(`ddl-auto=create-drop`), 그 뒤에 `src/main/resources/db/h2/data.sql`이 샘플 데이터를 넣습니다. 테이블 구조를 바꾸려면 엔티티의 `@Column`·`@Table`을 고칩니다.

React 개발 서버에서 호출할 때는 React 개발 서버의 프록시 기능으로 `/api`를 `http://localhost:8080`에 연결합니다. 운영에서는 같은 도메인(Apache 경유)이라 CORS 설정이 필요 없습니다.

## 빌드

```bash
./gradlew clean bootJar
```

- 결과물: `build/libs/spring-petclinic-4.0.0-SNAPSHOT.jar` (실행 가능한 JAR 하나만 만듭니다. `-plain.jar`는 만들지 않습니다)
- Windows에서는 `gradlew.bat clean bootJar`를 씁니다.
- 자동 테스트는 없습니다. 엔티티나 샘플 데이터를 바꾼 뒤에는 로컬(H2)과 RDS(또는 로컬 MySQL)에서 API를 직접 호출해 확인합니다.

## MySQL로 실행

`mysql` 프로파일을 켜고 접속 정보는 환경 변수로 넘깁니다.

```bash
SPRING_PROFILES_ACTIVE=mysql SPRING_DATASOURCE_URL=jdbc:mysql://<DB 엔드포인트>:3306/petclinic SPRING_DATASOURCE_USERNAME=petclinic SPRING_DATASOURCE_PASSWORD=<비밀번호> java -jar build/libs/spring-petclinic-4.0.0-SNAPSHOT.jar
```

- DB(`petclinic`)가 없으면 접속할 때 만들고(`createDatabaseIfNotExist=true`), 테이블은 Hibernate가 엔티티와 비교해 **없는 테이블·컬럼만 추가**합니다(`ddl-auto=update`). 기존 데이터와 컬럼은 지우거나 바꾸지 않습니다.
- 그래서 앱 DB 계정에는 데이터 권한(`SELECT, INSERT, UPDATE, DELETE`)에 더해 `CREATE, ALTER, INDEX, REFERENCES` 권한이 필요합니다.
- 샘플 데이터(`db/mysql/data.sql`)는 자동으로 넣지 않습니다(`spring.sql.init.mode=never`). 빈 DB에 처음 한 번만 넣으려면 `SPRING_SQL_INIT_MODE=always`를 잠깐 주고 기동한 뒤 다시 뺍니다(일반 `INSERT`라 두 번 실행하면 중복 오류).

## 서버(EC2)에 배포

Amazon Linux 2023 기준입니다. 원본 EC2 한 대에서 이 레포를 받아 빌드·설치하고, AMI로 만들어 같은 WAS를 여러 대 띄웁니다. 접속은 SSM Session Manager를 사용합니다.

| 파일 | 역할 |
| --- | --- |
| `deploy/build-and-deploy.sh` | git·JDK 설치 → `git pull` → `./gradlew` 빌드 → `setup-was.sh` 실행 |
| `deploy/setup-was.sh` | Corretto 21 설치, `petclinic` 계정 생성, JAR·시작 스크립트·서비스 설치 후 시작 |
| `deploy/start.sh` | Parameter Store에서 DB 접속 정보를 읽어 환경 변수로 넘기고 Java 실행 → `/opt/petclinic/start.sh` |
| `deploy/petclinic.service` | systemd 서비스 → `/etc/systemd/system/` (부팅 시 자동 시작, 비정상 종료 시 10초 뒤 재시작, 시간대 `Asia/Seoul`) |

### DB 접속 정보 (Parameter Store)

DB 접속 정보는 서버 파일에 두지 않고 Systems Manager Parameter Store에 둡니다. `start.sh`가 서비스를 시작할 때마다 읽어 Java 프로세스의 환경 변수로만 넘기므로 디스크와 AMI에 남지 않습니다. 이 방식을 고른 이유는 `start.sh` 주석에 있습니다.

| 파라미터 (모두 SecureString, `alias/aws/ssm`) | 넘겨지는 환경 변수 | 값 |
| --- | --- | --- |
| `/petclinic/db/url` | `SPRING_DATASOURCE_URL` | `jdbc:mysql://<RDS 엔드포인트>:3306/petclinic` |
| `/petclinic/db/username` | `SPRING_DATASOURCE_USERNAME` | 앱 전용 DB 계정 |
| `/petclinic/db/password` | `SPRING_DATASOURCE_PASSWORD` | 비밀번호 |
| `/petclinic/db/type` | `SPRING_PROFILES_ACTIVE` | `mysql` |

IAM 권한은 따로 추가하지 않습니다. SSM 접속용으로 WAS 역할에 붙인 AWS 관리형 정책 `AmazonSSMManagedInstanceCore`에 `ssm:GetParameters` 권한이 들어 있고, 기본 키 `alias/aws/ssm`으로 암호화한 값은 `kms:Decrypt` 권한 없이 복호화됩니다. 이 정책은 계정의 모든 파라미터를 읽을 수 있으므로 다른 비밀값을 Parameter Store에 둘 때는 이 점을 고려합니다.

비밀번호를 바꿀 때는 Parameter Store 값을 고친 뒤 `sudo systemctl restart petclinic`(또는 ASG 인스턴스 새로 고침)만 하면 됩니다. AMI는 다시 만들 필요가 없습니다.

라이브러리는 따로 설치하지 않습니다. `./gradlew`가 `build.gradle`에 적힌 라이브러리를 내려받아 실행 가능한 JAR 안에 모두 넣으므로, 서버에는 빌드용 git·JDK와 실행용 Java만 있으면 됩니다.

1. EC2에 SSM Session Manager로 접속해 레포를 받습니다. (EC2에서 인터넷으로 나갈 수 있어야 합니다)

   ```bash
   sudo dnf install -y git
   git clone https://github.com/kdt-6-aws-3/middle-project-was.git
   ```

2. Parameter Store의 DB 접속 정보를 이 서버에서 읽을 수 있는지 확인합니다. 파라미터 이름 4개가 나오고 `InvalidParameters`가 비어 있으면 됩니다.

   ```bash
   aws ssm get-parameters --region ap-northeast-2 --names /petclinic/db/url /petclinic/db/username /petclinic/db/password /petclinic/db/type --query '{found: Parameters[].Name, missing: InvalidParameters}'
   ```

3. 빌드·설치 스크립트를 실행합니다. 서비스를 시작하고 헬스 체크가 `UP`이 될 때까지 최대 300초 기다립니다.

   ```bash
   sudo bash middle-project-was/deploy/build-and-deploy.sh
   ```

4. `sudo reboot` 뒤에도 자동으로 뜨는지 확인한 다음 AMI를 만듭니다.

- 로그: `journalctl -u petclinic -f`. Parameter Store를 읽지 못하면 원인이 여기에 찍히고, 10초마다 다시 시도합니다.
- 코드를 고친 뒤에는 푸시하고 3번 명령만 다시 실행하면 됩니다. 바꾼 뒤에는 AMI를 새로 만듭니다.
- 예전 방식(`/etc/petclinic/petclinic.env`)으로 설치한 서버는 전환 후 `sudo rm -r /etc/petclinic`로 지웁니다. 지우기 전에 만든 AMI와 스냅샷에는 비밀번호가 남아 있습니다.
- 시간대를 `Asia/Seoul`로 두는 이유: 생일·진료 날짜 검증의 "오늘"이 서버 시간대를 따르는데, EC2 기본값(UTC)이면 한국 시간 00~09시에 하루가 어긋납니다.
- EC2 IAM 역할에 `AmazonSSMManagedInstanceCore`가 필요합니다. SSM 접속과 Parameter Store 읽기에 함께 쓰입니다. SSH(22) 인바운드는 열지 않습니다.

## Docker 이미지로 실행 (GHCR)

빌드된 이미지는 조직의 GitHub Container Registry에 있습니다. `linux/amd64`(EC2)와 `linux/arm64`(Apple Silicon) 모두 들어 있어 실행하는 곳에 맞는 이미지가 자동으로 받아집니다.

| 항목 | 값 |
| --- | --- |
| 이미지 | `ghcr.io/kdt-6-aws-3/container-project-was` |
| 태그 | `0.0.2` (시맨틱 버전) |
| 패키지 페이지 | <https://github.com/orgs/kdt-6-aws-3/packages/container/package/container-project-was> |
| 컨테이너 포트 | 8080 (헬스 체크 `/actuator/health`) |
| 실행 계정 | 비root `petclinic` |

### 1. 레지스트리 로그인

패키지가 private이라 받기 전에 한 번 로그인합니다. 비밀번호 자리에는 GitHub 비밀번호가 아니라 `read:packages` 권한이 있는 Personal access token(classic)을 넣습니다.

```bash
docker login ghcr.io -u <GitHub 사용자명>
```

`gh` CLI를 쓰면 토큰을 복사하지 않아도 됩니다.

```bash
gh auth login --scopes read:packages
gh auth token | docker login ghcr.io -u "$(gh api user -q .login)" --password-stdin
```

### 2. 받기와 실행

```bash
docker pull ghcr.io/kdt-6-aws-3/container-project-was:0.0.2
```

접속 정보는 이미지에 들어 있지 않으므로 환경 변수로 넘깁니다. [.env.example](.env.example)을 `.env`로 복사해 값을 채운 뒤 실행합니다. `.env`는 커밋하지 않습니다.

```bash
cp .env.example .env
docker run -d --name petclinic-was -p 8080:8080 --env-file .env ghcr.io/kdt-6-aws-3/container-project-was:0.0.2
```

| 환경 변수 | 설명 | 이미지 기본값 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `mysql`이면 MySQL, 비우면 H2(메모리) | `mysql` |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://<DB 호스트>:3306/petclinic` | 없음 |
| `SPRING_DATASOURCE_USERNAME` | 앱 전용 DB 계정 | 없음 |
| `SPRING_DATASOURCE_PASSWORD` | 비밀번호 | 없음 |
| `LOG_LEVEL` | `DEBUG` · `INFO` · `WARN` · `ERROR` | `INFO` |
| `APP_ENV` | 로그 JSON의 `env` 값 | `production` |
| `JAVA_OPTS` | JVM 옵션 | `-XX:MaxRAMPercentage=75` |
| `TZ` | 컨테이너 시간대 (예약 날짜 검증 기준) | `Asia/Seoul` |

DB 없이 API만 확인하려면 H2로 띄웁니다.

```bash
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=default ghcr.io/kdt-6-aws-3/container-project-was:0.0.2
```

### 3. 확인

```bash
docker ps --filter name=petclinic-was   # STATUS에 (healthy)가 보이면 정상
curl -i http://localhost:8080/actuator/health
docker logs -f petclinic-was
```

- 로그는 한 줄에 JSON 하나(`timestamp`, `level`, `service`, `env`, `traceId`, `requestId` 등)로 출력됩니다.
- 요청 헤더 `X-Trace-Id`를 보내면 그 값을, 없으면 새로 만든 값을 로그와 응답 헤더 `X-Trace-Id`·`X-Request-Id`에 남깁니다.

### 새 버전 올리기

이미지 빌드에 쓰는 `Dockerfile`은 팀 결정에 따라 `.gitignore`로 저장소에서 제외되어 있습니다. 새 버전은 Dockerfile을 가진 담당자가 버전을 올려 푸시합니다(`write:packages` 권한 필요).

```bash
docker buildx build --platform linux/amd64,linux/arm64 \
  --build-arg VERSION=<버전> \
  -t ghcr.io/kdt-6-aws-3/container-project-was:<버전> --push .
```

버전은 시맨틱 버전(`MAJOR.MINOR.PATCH`)을 따릅니다. 이미 올린 태그는 덮어쓰지 않고 새 번호를 씁니다.

## 원본 프로젝트

이 프로젝트는 [spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic)(Apache License 2.0)에서 출발했습니다. 원본의 설계 설명은 [발표 자료](https://speakerdeck.com/michaelisvy/spring-petclinic-sample-application)를 참고하세요.

## License

The Spring PetClinic sample application is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
