<a id="top"></a>
<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=24&height=250&section=header&text=NEIGHBUS%20APP&fontSize=80&animation=fadeIn&fontAlignY=38&desc=Connecting%20Neighbors,%20Building%20Community&descAlignY=55&descAlign=50" width="100%"/>

<br/> 

### 이웃과 함께 성장하는 커뮤니티 플랫폼 (React Native)

<br/>

[![Typing SVG](https://readme-typing-svg.demolab.com?font=Fira+Code&size=24&duration=3000&pause=1000&color=2E8B57&center=true&vCenter=true&multiline=true&width=600&height=120&lines=Neighborhood+%2B+Bus+%3D+NEIGHBUS;++(%EC%9D%B4%EC%9B%83)++++%2B+(%ED%86%B5%ED%95%98%EB%8B%A4);%EC%9D%B4%EC%9B%83%EA%B3%BC+%ED%86%B5%ED%95%98%EB%8A%94+%EB%94%B0%EB%9C%BB%ED%95%9C+%EC%BB%A4%EB%AE%A4%EB%8B%88%ED%8B%B0)](https://git.io/typing-svg)

<br/>

[![React Native](https://img.shields.io/badge/React_Native-20232A?style=flat-square&logo=react&logoColor=61DAFB)](https://reactnative.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![React Navigation](https://img.shields.io/badge/React_Navigation-6B52AE?style=flat-square&logo=react&logoColor=white)](https://reactnavigation.org/)
[![Stomp.js](https://img.shields.io/badge/Stomp.js-F05033?style=flat-square&logo=git&logoColor=white)](https://stomp-js.github.io/)

</div>

---
<br/><br/>

## 프로젝트 개요

<div align="center">

### "당신의 이웃이 당신의 친구가 되는 순간"

</div>

<br/>

**NEIGHBUS**는 기술을 통해 이웃 간의 단절을 해결하고, 오프라인 만남으로 확장되는 진정한 커뮤니티를 구축하는 것을 목표로 하는 모바일 애플리케이션입니다.

<br/>

<div align="center">

| **개발 기간** | **팀 구성** | **목표** | **결과** |
| :---: | :---: | :---: | :---: |
| 2025.11 ~ 2025.12 | 4인 | 지역 커뮤니티 활성화 앱 | MVP 완성 |

</div>

<br/><br/><br/>

<div align="center">

### "함께 성장하는 개발자들"

<br/>

<table>
<tr>
<td align="center" width="25%">
<a href="https://github.com/wjdgkr3386" target="_blank">
<img src="https://github.com/wjdgkr3386.png" width="120" style="border-radius:50%"/></a><br/>
<b>김정학</b><br/>
<sub>시큐리티 / AWS(S3)</sub>
</td>
<td align="center" width="25%">
<a href="https://github.com/happycat12389" target="_blank">
<img src="https://github.com/happycat12389.png" width="120" style="border-radius:50%"/></a><br/>
<b>김기현</b><br/>
<sub>회원가입 / 모임</sub>
</td>
<td align="center" width="25%">
<a href="https://github.com/library-min" target="_blank">
<img src="https://github.com/library-min.png" width="120" style="border-radius:50%"/></a><br/>
<b>서재민</b><br/>
<sub>관리자 / UX/UI </sub>
</td>
<td align="center" width="25%">
<a href="https://github.com/Mingyu7" target="_blank">
<img src="https://github.com/Mingyu7.png" width="120" style="border-radius:50%"/></a><br/>
<b>이민규</b><br/>
<sub>웹 소켓 / 소셜 로그인</sub>
</td>
</tr>
</table>

</div>

<br/><br/>

---
<br/><br/>

## 핵심 가치

<div align="center">

### "단순한 앱을 넘어, 사회적 연결을 위한 플랫폼"

<br/>

<table>
<tr>
<td align="center" width="33%">
<img src="https://fonts.gstatic.com/s/e/notoemoji/latest/1f91d/512.gif" width="96"/>
<h3>연결</h3>
<p>온라인에서 오프라인으로 확장되는 이웃 커뮤니티</p>
</td>
<td align="center" width="33%">
<img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Travel%20and%20places/Globe%20Showing%20Europe-Africa.png" width="96"/>
<h3>지역성</h3>
<p>내 주변의 검증된 장소와 모임 정보</p>
</td>
<td align="center" width="33%">
<img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Travel%20and%20places/Rocket.png" width="96"/>
<h3>사용자 경험</h3>
<p>네이티브처럼 부드럽고 직관적인 사용성</p>
</td>
</tr>
</table>

</div>

<br/>

---
<br/><br/>

## 기술적 도전과 해결

<div align="center">

### "React Native 환경에서의 기술적 문제 해결 기록"

</div>

<br/>

###  Challenge 1: 실시간 채팅 기능 구현

> **과제**
> React Native 환경에서 끊김 없는 안정적인 실시간 채팅을 구현해야 했습니다.

- **해결**: **`@stomp/stompjs`**와 **`WebView`**를 결합하여 하이브리드 방식으로 해결했습니다.
  - `WebView` 내에서 WebSocket 연결 및 STOMP 프로토콜 처리를 전담하도록 브릿지 생성.
  - React Native는 `WebView`와 `postMessage`로 통신하여 네이티브 의존성 최소화.
- **결과**: 복잡한 네이티브 모듈 설정 없이 **안정적인 실시간 채팅 기능**을 구현하고 앱의 리소스 부담을 줄였습니다.

<br/>

###  Challenge 2: 지오코딩(Geocoding) 적용

> **과제**
> 모임 장소 선택 시, 지도에 표시된 좌표(위도/경도)를 사용자가 이해하기 쉬운 '주소 텍스트'로 변환해야 했습니다.

- **해결**: **`react-native-maps`**와 **`Google Maps Geocoding API`**를 연동했습니다.
  - `react-native-maps` UI에서 사용자가 핀을 이동시켜 정확한 좌표 획득.
  - 해당 좌표를 **Google Geocoding API**에 비동기 전송하여 도로명 주소로 변환.
- **결과**: 단순 지도 표시를 넘어, **지오코딩(Geocoding)** 기능을 통해 사용자가 모임 장소를 직관적으로 설정할 수 있게 되었습니다.

<br/>

---
<br/><br/>

## 주요 기능

| 기능 | 설명 |
| :--- | :--- |
| **홈** | 카테고리별 동아리 목록, 신규 모임 및 인기 게시글 표시 |
| **인증** | 이메일/비밀번호 기반 로그인 및 회원가입, 자동 로그인 |
| **동아리** | 동아리 생성, 검색, 가입 및 상세 정보 확인 |
| **모임** | 동아리 내 오프라인 모임 생성, 지도 기반 장소 지정 |
| **실시간 채팅** | STOMP + WebSocket 기반 실시간 채팅 및 친구 관리 |
| **게시판** | 자유게시판, 갤러리, 공지사항 기능 (글, 댓글, 반응) |
| **마이페이지** | 내 정보 수정, 가입한 동아리 및 작성 글 목록 확인 |

<br/>

---
<br/><br/>

##  아키텍처

###  기술 스택

| Category | Technologies |
| :--- | :--- |
| **Core** | `React`, `React Native`, `TypeScript` |
| **Navigation** | `React Navigation` |
| **State & Data** | `AsyncStorage`, `React Context API` |
| **Real-time** | `@stomp/stompjs`, `react-native-webview` |
| **Map & Location**| `react-native-maps`, `Google Maps Geocoding API` |
| **DevOps** | `ESLint`, `Prettier` |

<br/>

###  네비게이션 흐름

`React Navigation`을 사용하여 앱의 화면 전환을 체계적으로 관리합니다.

```mermaid
graph TD
    %% 스타일 정의 (파스텔 톤)
    classDef entry fill:#333,stroke:#333,stroke-width:2px,color:#fff,rx:10,ry:10;
    classDef auth fill:#ffcdd2,stroke:#e57373,stroke-width:2px,color:#333,rx:5,ry:5;
    classDef main fill:#bbdefb,stroke:#64b5f6,stroke-width:2px,color:#333,rx:5,ry:5;
    classDef tab fill:#c8e6c9,stroke:#81c784,stroke-width:2px,color:#333,rx:5,ry:5;
    classDef sub fill:#f5f5f5,stroke:#bdbdbd,stroke-width:2px,color:#333,rx:5,ry:5,stroke-dasharray: 5 5;

    %% 시작점
    Start((📱 App Start)):::entry
    
    %% 로그인 체크
    Start --> Check{로그인 여부}
    
    Check -- No --> AuthStack
    Check -- Yes --> MainTab

    %% Auth Stack
    subgraph Auth_Flow [🔒 Auth Stack]
        direction TB
        AuthStack[Auth Navigator]:::auth
        AuthStack --> Welcome[👋 Welcome]:::auth
        AuthStack --> Login[👤 Login]:::auth
        AuthStack --> Signup[📝 Signup]:::auth
    end

    %% Main Tab
    subgraph Main_Flow [🏠 Main App]
        direction TB
        MainTab[Bottom Tab Navigator]:::main
        
        %% 탭 연결
        MainTab --> HomeTap[🏠 홈 탭]:::tab
        MainTab --> ChatTab[💬 채팅 탭]:::tab
        MainTab --> MyTab[👤 마이페이지 탭]:::tab

        %% 홈 탭 상세
        HomeTap --> ClubDetail[🎪 동아리 상세]:::sub
        HomeTap --> MeetingDetail[📅 모임 상세]:::sub
        HomeTap --> FreeBoard[📋 게시판]:::sub
        ClubDetail -.-> CreateMeeting[➕ 모임 생성]:::sub
    end

    %% 연결선 스타일
    linkStyle default stroke:#666,stroke-width:2px;
```

<br/>

##  시작하기

<div align="center">

### "5분 안에 로컬 환경에서 앱 실행하기"

</div>

<br/>

### 사전 요구사항

아래의 개발 도구들이 미리 설치되어 있어야 합니다. 각 도구의 이름(파란색 글씨)을 클릭하면 공식 설치 페이지로 이동합니다.

| Tool / Platform | Description | 설치 가이드 |
| :--- | :--- | :--- |
| **[Node.js](https://nodejs.org/)** | `v18` 이상의 LTS 버전을<br/>권장합니다. | `nvm` 또는 공식 설치<br/>프로그램을 사용하세요. |
| **[Watchman](https://facebook.github.io/watchman/)** | 파일 변경을 감지하여<br/>앱을 자동으로 새로고침합니다. | macOS: `brew install watchman` |
| **[Xcode](https://developer.apple.com/xcode/)** | iOS 시뮬레이터를 실행하고<br/>앱을 빌드하는 데 필요합니다. | Mac App Store에서 설치하세요. |
| **[Android Studio](https://developer.android.com/studio)** | Android 시뮬레이터를 실행하고<br/>앱을 빌드하는 데 필요합니다. | 공식 홈페이지에서<br/>다운로드하세요. |
| **[CocoaPods](https://cocoapods.org/)** | iOS 네이티브 라이브러리<br/>의존성을 관리합니다. | `sudo gem install cocoapods` |

> React Native 개발 환경 설정에 대한 더 자세한 내용은 [공식 문서](https://reactnative.dev/docs/environment-setup)를 참고하시면 큰 도움이 됩니다.

<br/>

### 설치 및 실행

프로젝트를 로컬 환경에 설정하고 실행하는 과정입니다. 터미널에서 아래 명령어를 순서대로 입력해주세요.

<br/>

<table>
  <thead>
    <tr>
      <th width="10%">Step</th>
      <th width="35%">Command</th>
      <th width="55%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center">1</td>
      <td><code>git clone </code><br/><code>cd neighbusapp-main</code></td>
      <td>GitHub에서 프로젝트 소스코드를 받고 해당 폴더로 이동.</td>
    </tr>
    <tr>
      <td align="center">2</td>
      <td><code>npm install</code></td>
      <td><code>package.json</code>에 명시된 모든 JavaScript 의존성을 설치.</td>
    </tr>
    <tr>
      <td align="center">3</td>
      <td><code>cd ios && pod install && cd ..</code></td>
      <td>iOS 프로젝트에 필요한 네이티브 모듈 의존성을 설치.</td>
    </tr>
    <tr>
      <td align="center">4</td>
      <td><code>src/config.ts</code> 파일 수정</td>
      <td>코드 에디터에서 <code>src/config.ts</code> 파일을 열고,<br/><code>BASE_URL</code> 값을 실제 백엔드 서버의<br/>IP 주소나 도메인으로 변경합니다.</td>
    </tr>
    <tr>
      <td align="center">5</td>
      <td><code>.env</code> 파일 생성</td>
      <td>프로젝트 최상위 경로에 <code>.env</code> 파일을 생성하고,<br/>아래 내용을 추가합니다.<br/><code>GOOGLE_MAPS_API_KEY=발급받은_구글맵_API_키</code><br/><br/><b>참고:</b> <code>Google Cloud Console</code>에서<br/>아래 두 가지 API를 활성화해야 합니다.<br/>- <code>Geocoding API</code><br/>- <code>Maps SDK for Android</code></td>
    </tr>
    <tr>
      <td align="center">6</td>
      <td>앱 실행</td>
      <td>아래 명령어를 사용하여 개발 서버를 시작하고<br/>앱을 빌드합니다.<br/><br/><b>Android:</b><br/><code>npx react-native run-android</code><br/><br/><b>iOS:</b><br/><code>npm run ios</code><br/></td>
    </tr>
  </tbody>
</table>

<br/>

---

<div align="center">

<br/>

**Made with ❤️ by NEIGHBUS Team**

<br/>

![Footer](https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=24&height=150&section=footer)

<br/>

[⬆️ Back to Top](#top)

</div>
