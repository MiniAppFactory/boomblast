# Boom Blocks Changelog

## [1.0.8] - 2026-08-16 (Faz 114, Play politika uyumu)

**versionCode 9 → 10, versionName 1.0.7 → 1.0.8.**
vc9/1.0.7 Play'e yüklendi (kullanıcı teyidi — handover'daki "vc9 hiç
yüklenmedi" notu yanlıştı). Yayınlanmış bir versionName'e farklı bir ikili
vermemek için ad da artırıldı; aynı gerekçe 1.0.4 → 1.0.5'te de uygulanmıştı.

Kapatılan iki **Play politikası** maddesi. İkisi de reklam gelirini
azaltmıyor; yalnızca yaptırım riskini kaldırıyor.

### Fixed
- **Geçiş reklamı sıklık sınırı (TODO madde 1)** — interstitial'in zaman bazlı
  hiçbir üst sınırı yoktu. Pro Mod'da üst üste kaybeden oyuncu
  "YENIDEN BAŞLA"/"HARİTAYA DÖN" döngüsünde ~30-60 saniyede bir tam ekran
  reklam görüyordu, tavan yoktu. Play'in "disruptive ads" politikasının
  yaptırım alanı.
  - Yeni `ads/InterstitialFrequencyPolicy.kt` — saf karar mantığı, Android
    bağımlılığı yok, JVM'de test edilebilir
  - `MIN_INTERVAL_MS = 60_000` iki gerçek gösterim arası en kısa süre;
    `SESSION_GRACE_MS = 45_000` uygulama açılışından sonra reklamsız pencere
    (ilk oturum tamamen korumasızdı)
  - Sınır 12 çağrı noktasının her birine değil, hepsinin geçtiği **tek boğaz
    noktasına** (`InterstitialAdManager`) kondu — yeni çağrı noktası eklenirse
    otomatik korunur
  - Cooldown **yalnızca gerçekten gösterilip kapatılan** reklamdan sonra başlar;
    no-fill (bu projede 36× ölçülmüş) cooldown'ı yakmaz, yoksa üst üste
    başarısız yüklemeler gerçek gösterimleri sessizce kilitlerdi
  - Sınır devredeyken oyuncu beklemez, reklam atlanır ve akış aynen sürer

- **Bildirim metni reklam izlemeye çağırıyordu (TODO madde 6)** —
  `NotificationHelper.kt`'de 3. mesaj beş dilde de "Reklam izle, güçlendirici
  al" / "Watch an ad, get a booster" diyordu. Play, bildirimlerin
  reklam/promosyon aracı olarak kullanılmasını ayrıca yasaklar; sıklık
  ihlalinden farklı olarak bu tek bir bildirimde bile manuel incelemede göze
  çarpar. Beş dilde de nötr hatırlatmaya çevrildi. Oyuncu oyuna girince
  rewarded butonunu zaten görüyor — gelir yolu kapanmıyor.

### Added
- `InterstitialFrequencyPolicyTest` (10 test) — TODO madde 1'deki somut
  hızlı-ateş senaryosu dahil
- `NotificationHelperPolicyTest` (3 test) — beş dilde reklam çağrısı arar;
  metin düzeltmesi kolaydır, asıl risk ileride benzer bir mesajın iyi niyetle
  geri eklenmesidir

### Verified
- `testDebugUnitTest`: **49 test / 0 hata** (13'ü yeni)
- `bundleRelease` + `signReleaseBundle`: BUILD SUCCESSFUL
- `aapt2 dump badging`: `versionCode='10' versionName='1.0.8'` doğrulandı
- İmza: `CN=Blast the Blocks, OU=AppDeveloper, O=AppDeveloper, L=Istanbul, C=TR`
- **AAB içeriği doğrulandı** — R8 açık olduğu için sınıf adları obfuscate;
  bunun yerine obfuscate edilmeyen string literal'lere bakıldı: yeni bildirim
  metinlerinin **dördü de AAB'de var**, eski "Watch an ad, get a booster" /
  "Time to grab free tokens" **yok**. `InterstitialFrequencyPolicy` mapping'de
  görünmüyor çünkü R8 inline etmiş; release `.class` dosyası derleme
  çıktısında mevcut ve kaynak düzenlemesinden sonraya tarihli.
- Artefaktlar: AAB 8.9 MB, signed APK 4.71 MB (16-Aug-26 22:15)

### Denetlendi — sorun bulunmadı
- `AD_ID` izni SDK'dan otomatik geliyor, Data Safety'deki Advertising ID
  beyanıyla tutarlı
- İzinler: INTERNET, ACCESS_NETWORK_STATE, POST_NOTIFICATIONS, VIBRATE —
  gereksiz/tehlikeli izin yok
- Gizlilik politikası canlı ve doğru (`miniappfactory.github.io/boomblast`),
  iletişim `whatsthisapp@proton.me`, "not designed for children" ibaresi var;
  hedef kitle 13+ olarak teyit edildi → Families politikası devrede değil
- App-open reklam yok

### Açık (kod dışı)
- Mağaza görsellerinde üç maddi hata (`LEVEL`→`CAREER`, olmayan "3 lives",
  3 görev↔5 görev). Düzeltilmiş `*_v2.png` dosyaları hazır, Play Console'a
  elle yüklenmeli. Aciliyeti düşük.
- `screenshot_1_gameplay.png` Faz 112 öncesi HUD'ı gösteriyor.

## [1.0.7] - 2026-08-16 (Faz 112-113, Closed Testing)

### Changed
- **HUD Complete Redesign (Faz 112)**
  - Implemented 2×3 invisible grid layout: power-ups (left, vertical stack), centered SCORE (math-based, not weight-based), TARGET score (right edge)
  - SCORE now anchored to exact horizontal center using `Box(fillMaxWidth, contentAlignment=Center)` — immune to booster count or target text width changes
  - Power-ups extracted to independent TopStart column with `padding(start=16.dp)`, true left-edge alignment
  - Fixed HUD container height (96.dp) prevents grid repositioning
  - Removed FloatingScoreManager (flying score animation) — caused noticeable performance lag (frame drops during multi-block clears)
  
- **Visual Polish**
  - Grid cell padding reduced: 1.5.dp → 0.5.dp
  - Grid border stroke thinned: 0.75.dp → 0.5.dp (thinner visual lines, still visible)
  - Disabled radial beam/lazer flash effects (line 781: `radialBeamCount = 0`)

- **Settings Menu Reorganization (Faz 112)**
  - Theme Dropdown moved to top (Material 3 DropdownMenu with BLOCK_THEMES)
  - Appearance (Görünüm) Dropdown repositioned to top, skin selector no longer a LazyRow gallery at bottom
  - Reordered menu: Sound Effects → Volume → Theme → Appearance → Mode → Music → Haptics → Notifications → How to Play → Privacy → Language
  - Removed Music switch row (deemed unnecessary)

- **Game Economy**
  - Puan şartları reverted to static formula: `100 + (n - 1) * 5` (was dynamic 200+(n-1)*40)
  - Level progression stabilized across all difficulty tiers

### Technical
- **Build**: versionCode 8 → 9, versionName 1.0.6 → 1.0.7
- **Signed Artifacts**: APK 4.71 MB, AAB 8.90 MB
- **Git**: Commit bb9a1ad (Faz 113), pushed to MiniAppFactory/boomblast master

### Verified
- S8 device testing: HUD layout stable across orientation changes, grid geometry unaffected
- Build: Gradle successful, no breaking lint errors (existing baseline applied)
- Performance: Frame drops during multi-block clears resolved post-FloatingScoreManager removal
