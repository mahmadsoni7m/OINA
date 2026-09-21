# OINA — Digital Mirror

Барномаи Android, ки телефонро ба оинаи рақамӣ табдил медиҳад. 100% offline: на интернет, на аккаунт, на сервер, на tracking.

**Developer:** SONI

---

## 🧱 Меъмории лоиҳа

- **Забон:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Камера:** CameraX (Preview + ImageCapture)
- **Захира:** DataStore Preferences (локалӣ, дар дастгоҳ)
- **Навигатсия:** Navigation-Compose (Mirror ↔ Settings)
- **Меъморӣ:** MVVM — `SettingsRepository` (DataStore) → `SettingsViewModel` (StateFlow) → Compose UI
- **Min SDK:** 26 (Android 8.0) · **Target SDK:** 34

Namoиши оина комилан дар сатҳи `PreviewView` (scaleX = -1) иҷро мешавад — ягон коркарди frame-ба-frame нест, аз ин рӯ latency хеле кам аст. Акси захирашуда мустақилона мувофиқи танзимоти "Захира кардани акси оинавӣ" flip карда мешавад.

## 📁 Сохтори project

```
OINA/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── .gitignore
├── README.md
├── .github/
│   └── workflows/
│       └── build.yml
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/soni/oina/
        │   ├── OinaApp.kt
        │   ├── MainActivity.kt
        │   ├── camera/
        │   │   └── CameraController.kt
        │   ├── navigation/
        │   │   └── OinaNavHost.kt
        │   ├── settings/
        │   │   ├── SettingsRepository.kt
        │   │   └── SettingsViewModel.kt
        │   ├── ui/
        │   │   ├── theme/ (Color.kt, Type.kt, Theme.kt)
        │   │   ├── components/ (CameraPreviewView.kt, ControlBar.kt,
        │   │   │                 ZoomControl.kt, PermissionRationale.kt)
        │   │   └── screens/ (MirrorScreen.kt, SettingsScreen.kt)
        │   └── utils/
        │       └── ImageSaver.kt
        └── res/
            ├── values/ (strings.xml, colors.xml, themes.xml)
            ├── drawable/ (ic_launcher_background.xml, ic_launcher_foreground.xml, ic_launcher_legacy.xml)
            ├── mipmap-anydpi-v26/ (ic_launcher.xml, ic_launcher_round.xml)
            └── xml/ (backup_rules.xml, data_extraction_rules.xml)
```

## 📲 Функсияҳо

- Camera preview бо mirror mode (ON/OFF аз Settings)
- Front ↔ Back switch (агар камера мавҷуд набошад, crash намекунад)
- Pinch-to-zoom + zoom slider (аз min/max воқеии дастгоҳ)
- Screen Light — экранро равшан мекунад барои истифода дар торикӣ
- Capture → захира ба Gallery/MediaStore (`Pictures/OINA`), бо гузинаи "захира ба таври оина"
- Settings: Camera, Appearance (Dark/Light/System), Display (Keep Screen On + Brightness), About
- Permission handling бо rationale ва тугмаи "Кушодани Танзимот" агар рад карда шавад

## 🚀 Аз GitHub Actions APK гирифтан (Termux workflow)

Азбаски шумо аз телефон + Termux кор мекунед ва Android Studio надоред, лоиҳа тавре сохта шудааст, ки **GitHub Actions** тамоми build-ро дар cloud иҷро кунад — шумо ба compile дар телефон эҳтиёҷ надоред.

### 1) Repository сохтан ва project-ро upload кардан

Дар Termux:

```bash
cd OINA
git init
git add .
git commit -m "OINA: initial project"
git branch -M main
git remote add origin https://github.com/<USERNAME>/OINA.git
git push -u origin main
```

(`<USERNAME>`-ро бо номи GitHub-и худ иваз кунед; пеш аз ин дар github.com як repository холӣ бо номи `OINA` созед.)

### 2) GitHub Actions-ро оғоз кардан

Ҳамин ки push кунед, workflow худкор оғоз мешавад (файли `.github/workflows/build.yml`). Инчунин метавонед дастӣ оғоз кунед:

1. Ба repository дар GitHub равед → tab **Actions**
2. Workflow **Build OINA APK**-ро интихоб кунед
3. Тугмаи **Run workflow** → **Run workflow**

### 3) APK-ро download кардан

1. Дар tab **Actions**, run-и охиринро кушоед (нишонаи ✅ сабз)
2. Дар поён, қисми **Artifacts** ду файл мебинед:
   - `OINA-debug-apk` — барои санҷиш
   - `OINA-release-apk` — барои паҳн кардан
3. Онро download кунед (файл `.zip` мешавад — дар дохилаш `.apk` ҳаст)

### 4) APK-ро дар телефон install кардан

1. Файли `.zip`-и download-шударо кушоед, `.apk`-ро бароред
2. Дар телефон: **Танзимот → Апп-ҳо → Насби апп-ҳои номаълум** — барои браузер/файл-менеҷери худ иҷозат диҳед
3. Ба файли `.apk` tap кунед → **Install**
4. Пас аз насб, OINA-ро кушоед ва ба камера иҷозат диҳед

> **Тавзеҳ:** Release APK бо debug signing config имзо шудааст (барои осонии build дар Actions бе keystore). Барои паҳн кардани расмӣ дар Play Store, keystore-и худро созед ва онро ба `signingConfigs` дар `app/build.gradle.kts` ҳамроҳ кунед.

## 🛠 Санҷиши маҳаллӣ (ихтиёрӣ, агар PC бо Android Studio дошта бошед)

```bash
git clone https://github.com/<USERNAME>/OINA.git
cd OINA
./gradlew assembleDebug
```

(Дар ин ҳолат Android Studio худаш `gradlew`-ро генератсия мекунад — дар CI мо аз `gradle/actions/setup-gradle` истифода мебарем, аз ин рӯ гузоштани wrapper зарур нест.)
