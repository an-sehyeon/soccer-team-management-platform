# 05_tech_stack_rules.md

# 기술 스택 및 개발 규칙

## 기술 스택

이 프로젝트는 Spring Boot와 React를 기반으로 개발한다.

## Backend

백엔드는 다음 기술을 사용한다.

* Java
* Spring Boot
* Gradle
* Spring Security
* JPA
* MySQL
* IntelliJ IDEA

## Frontend

프론트엔드는 다음 기술을 사용한다.

* React
* VS Code
* 반응형 UI

JavaScript와 TypeScript 중 어떤 것을 사용할지는 프로젝트 초기에 확정한다.

사용자가 확정하기 전까지 임의로 TypeScript 사용을 확정하지 않는다.

## Database

DB는 MySQL을 사용한다.

DB 설계 시 다음을 반드시 고려한다.

* 팀 단위 데이터 분리
* 지도자/선수 권한 구분
* 선수 개인 영상 접근 제어
* 경기 영상 원본 관리
* 클립 메타데이터 관리
* 드로잉 데이터 관리
* 코멘트 데이터 관리
* 추후 AI 분석 결과 저장 가능성

## 영상 저장 방식

경기 영상은 원본 영상과 분석 클립을 분리해서 생각한다.

초기에는 실제 영상을 매번 잘라서 저장하는 방식보다, 원본 영상은 그대로 두고 클립의 시작 시간과 종료 시간을 메타데이터로 저장하는 방식을 우선 검토한다.

이유는 다음과 같다.

* 서버 저장 공간을 줄일 수 있다.
* 영상 인코딩 비용을 줄일 수 있다.
* 클립 생성 속도가 빠르다.
* 원본 영상 기준으로 여러 클립을 만들기 쉽다.

다만 실제 다운로드 가능한 클립 파일이 필요하다면 추후 인코딩 작업을 별도로 설계한다.

## 코드 작성 규칙

코드는 무조건 짧게 작성하는 것보다 가독성을 우선한다.

중학생이 봐도 흐름을 이해할 수 있을 정도로 명확하게 작성한다.

변수명, 메서드명, 클래스명은 역할이 드러나게 작성한다.

축약어는 되도록 피한다.

예를 들어 다음과 같은 이름을 선호한다.

```java
createMatchVideo()
findTeamAnalysisClips()
checkPlayerVideoPermission()
```

다음과 같이 의미가 불명확한 이름은 피한다.

```java
doSave()
getList()
chk()
```

## 주석 작성 규칙

클래스에는 이 클래스가 어떤 역할을 하는지 짧게 작성한다.

메서드에는 이 메서드가 어떤 기능을 하는지, 중요한 처리 흐름이 무엇인지 작성한다.

주석은 너무 길게 작성하지 않지만, 한눈에 역할과 이유를 알 수 있어야 한다.

예시:

```java
/**
 * 경기 영상 관리 서비스
 *
 * 지도자가 업로드한 경기 영상을 저장하고,
 * 팀 분석 영상과 선수 개인 분석 영상 생성을 위한 기본 데이터를 관리한다.
 *
 * 주요 역할
 * - 경기 영상 등록
 * - 경기 영상 조회
 * - 경기 영상 권한 검증
 * - 분석 클립 생성 전 원본 영상 확인
 */
@Service
@RequiredArgsConstructor
public class MatchVideoService {

    /**
     * 경기 영상 업로드
     *
     * 처리 흐름
     * 1. 로그인한 사용자가 지도자인지 확인한다.
     * 2. 업로드할 팀에 대한 권한이 있는지 확인한다.
     * 3. 영상 파일을 저장소에 저장한다.
     * 4. 영상 기본 정보를 DB에 저장한다.
     *
     * 주의사항
     * - 선수는 경기 영상을 업로드할 수 없다.
     * - 실제 파일 저장과 DB 저장 중 하나라도 실패하면 전체 처리를 실패로 본다.
     */
    public Long uploadMatchVideo(MatchVideoUploadRequest request) {
        // 구현 코드 작성
    }
}
```

## API 작성 규칙

API는 역할과 목적이 명확하게 드러나야 한다.

예시:

```text
POST /api/coach/teams/{teamId}/match-videos
GET /api/player/teams/{teamId}/team-analysis-clips
GET /api/player/me/personal-analysis-clips
```

지도자 API와 선수 API는 가능하면 구분한다.

단, 실제 설계 시에는 중복이 너무 많아지지 않도록 공통 API와 권한 검증 방식을 함께 고려한다.

## 권한 검증 규칙

권한 검증은 프론트엔드에서만 처리하지 않는다.

백엔드에서 반드시 검증한다.

특히 다음 기능은 권한 검증이 필수다.

* 경기 영상 업로드
* 영상 편집
* 팀 분석 영상 조회
* 선수 개인 분석 영상 조회
* 스케줄 등록/수정/삭제
* 공지사항 등록/수정/삭제

## 프론트엔드 개발 규칙

프론트엔드는 모바일과 PC를 모두 고려한다.

선수 화면은 모바일 우선으로 설계한다.

지도자 영상 편집 화면은 PC 사용성을 우선한다.

컴포넌트는 역할별로 분리한다.

예시:

```text
CoachSchedulePage
PlayerSchedulePage
MatchVideoUploadPage
VideoEditorPage
TeamAnalysisClipList
PersonalAnalysisClipList
```

## 개발 진행 규칙

기능을 개발하기 전에 먼저 해당 기능의 md 문서를 작성한다.

예를 들어 스케줄 기능을 개발하기 전에는 `schedule_requirements.md`를 먼저 작성한다.

문서에는 다음 내용이 포함되어야 한다.

* 기능 목적
* 사용자 역할
* 권한 정책
* 화면 흐름
* API 흐름
* DB 설계 방향
* 예외 상황
* 구현 순서

문서가 정리된 뒤에 코드 구현을 시작한다.
