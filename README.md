# 한줄일기 (OneLineDiary)

어딘가에서 누군가 남긴 밤하늘의 일기와 함께 나의 오늘을 한 문장으로 안전하게 기록하는 **Kotlin Multiplatform (Compose Multiplatform)** 기반 감성 일기 애플리케이션입니다.

---

## 🌟 주요 기능 및 최신 사양

### 1. 현실적인 현대인의 문체로 교정된 가상 일기
- **자연스러운 구어체/독백체 ([`AnonymousDiaryGenerator.kt`](file:///c:/Users/AlphaGot/Documents/antigravity/gallant-pythagoras/composeApp/src/commonMain/kotlin/com/onelinediary/app/data/generator/AnonymousDiaryGenerator.kt))**:
  - 소설이나 에세이풍의 과장된 어투를 걷어내고, 2020~2026년대 현대인이 SNS나 비밀 메모장에 혼자 털어놓듯 담담하고 솔직한 어조(~했다, ~함, ~네, ~인 듯)로 전면 교정.
  - 퇴근길 만원 지하철, 번아웃, 편의점 캔맥주, 붕어빵 소확행, 인간관계의 피로와 서운함, 소소한 다짐 등 실제 공감 가는 일상의 감정을 생생하게 담아냈습니다.
- **동적 부유 일기 유영 및 연속 리스폰 (Drifting & Respawning Stream)**:
  - 밤하늘 배경에서 익명 일기들이 둥둥 떠서 부드러운 사인파 곡선을 그리며 서서히 유영합니다.
  - 일정 시간 유영 후 밤하늘 속으로 스며들어 완전히 사라지면, 1,000개의 일기 풀에서 **곧바로 새로운 무작위 일기가 튀어나오는 무한 순환 생명주기**를 가집니다.
  - 떠다니는 일기 풍선을 직접 터치해도 즉각 사라지며 새로운 일기가 튀어나옵니다.

### 2. 데스크톱(Desktop) 부팅 시 자동 시작 지원
- **Windows 자동 실행 연동 ([`PlatformAutoStart.kt`](file:///c:/Users/AlphaGot/Documents/antigravity/gallant-pythagoras/composeApp/src/commonMain/kotlin/com/onelinediary/app/data/autostart/PlatformAutoStart.kt))**:
  - 데스크톱 환경 구동 시 Windows 레지스트리(`HKCU\Software\Microsoft\Windows\CurrentVersion\Run`)와 직접 연동.
  - 설정 창에서 **"컴퓨터 부팅 시 자동 시작"** 스위치를 켜고 끌 수 있습니다.

### 3. '빨간약 (The Red Pill)' 이스터에그로 기술적 설정 은폐
- **AI 및 기술적 설정 비노출 원칙**:
  - 평상시 일반 사용자의 설정 화면에는 API 키나 모델 파라미터 같은 기계적 설정이 전혀 나타나지 않습니다.
- **빨간약 모드**:
  - 설정창 하단의 은밀한 `💊 빨간약` 버튼을 복용(클릭)하면 네온 레드 사이버펑크 무드와 함께 숨겨진 **시스템 코어(Gemini API 키 및 원격 프롬프트 수신 설정)**가 언락됩니다.
  - 언제든지 `🔵 파란약 먹기` 버튼으로 다시 기술 설정을 감추고 서정적인 평온 모드로 복귀할 수 있습니다.

### 4. 강력한 일기 암호화 및 보안 인증
- **최초 실행 시 암호 키(비밀번호) 지정**:
  - 마스터 암호 키는 **무작위 16바이트 솔트(Salt) + 1,000회 키 스트레칭(SHA-256 Key Stretching)**을 거쳐 단방향 해시로만 시스템에 안전하게 보관됩니다.
- **일기 본문 대칭키 암호화 (Symmetric Stream Encryption)**:
  - 디바이스 로컬 저장소에 저장되는 모든 일기 본문(`content`)은 사용자 비밀번호로부터 유도된 대칭키 스트림 암호화를 거쳐 난수화된 암호문(`ENC:...`)으로만 저장됩니다.
- **앱 즉시 잠금 (`🔒`) & 메모리 보안**:
  - 상단 바의 잠금 아이콘 클릭 시 즉시 앱이 잠기며, 메모리에 올라와 있던 복호화된 평문 일기 목록을 즉각 비웁니다(`clearMemory()`).

---

## 🛠️ 검증된 기술 스택

- **Kotlin 2.0.21** + **Compose Multiplatform 1.7.0**
- **Ktor Client 3.0.0** (WasmJs & Multiplatform 완전 호환)
- **CryptoUtils** (순수 코틀린 구현 SHA-256 + CTR 대칭키 암호화로 모든 플랫폼 동일 구동)
- **타깃 플랫폼**: Android, Desktop (Windows JVM), Web (Wasm), iOS

---

## 🚀 실행 및 테스트

- **단위 테스트 실행**:
  ```powershell
  .\gradlew :composeApp:desktopTest
  ```
- **Desktop 앱 실행 (암호화 + 부팅 자동시작 + 빨간약 체험)**:
  ```powershell
  .\gradlew :composeApp:run
  ```
- **Android / Web Wasm 빌드**:
  ```powershell
  .\gradlew :composeApp:compileKotlinWasmJs :composeApp:compileDebugKotlinAndroid
  ```
