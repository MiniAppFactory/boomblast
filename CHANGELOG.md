# Boom Blocks Changelog

## [Unreleased] - Faz 182 ("KOLAY MOD" kelime sanati duzlestirildi)

### Changed
- **`kb_esy_word.webp` yeniden uretildi (430x315 -> 430x175).** Kullanici:
  "kolay mod yazisi yanlis, pro ve kariyerin yazildigi gibi olmali, duz."
  Eldeki varlik IKI SATIRDI (KOLAY altin / MOD camgobegi + kup); PRO ve
  Kariyer tek satir altin. Iki satirlik varliktan "KOLAY"i kesip almak mumkun
  degildi -- iki kelimenin mor konturu tek govdede birlesmis.
  - Uretici betik: `tools/wordart/make_esy_word.py` (yeniden uretilebilir).
  - Tarz TAHMIN EDILMEDI: altin gradyan, mor kontur ve dip serit renkleri
    `kb_pro_word.webp`den PIKSEL ORNEKLENDI; font oyunun kendi basik fontu
    (`res/font/baloo2_extrabold.ttf`); parildamalar dogrudan PRO varligindan
    KESILDI (kenarlari yumusatilarak), yeniden cizilmedi.
  - En-boy 0.733 -> 0.407. `Ref.WORD` 0.402 varsayiyor; eski oran kelime
    sanatini yukari kaydiriyordu, artik Kariyer/Pro ile ayni hizada.

### Evidence
- `gradlew :app:assembleDebug` BUILD SUCCESSFUL
- Cihaz (SM-G965): Kolay Mod haritasi ekran goruntusu -- kelime tek satir,
  altin, Pro/Kariyer ile ayni olcu ve hizada.

## [Unreleased] - Faz 181 (Harita hedef haplari + basarisiz diyalogu yuksekligi)

### Changed
- **Harita: 1. seviyenin hedef hapi artik istisna degil.** Once dugumun ALTINDA,
  yukari bakan kuyruklu `pillUp` varligiyla duruyordu; simdi digerleri gibi
  dugumle AYNI HIZADA, yaninda. Tek seviye = SOL, cift seviye = SAG, yani
  1-2-3-4 sirasi duzgun alterniyor (`ProgressionMapScreen.TargetPillContinuous`).
- **Harita: hedef metni iki satir.** "HEDEF: 100 + 1 satir" tek satirda hapi
  gereginden genis birakiyordu. Artik puan ust satirda, "+ 1 satir" kurali alt
  satirda; satir yuksekligi puntoya kelepcelendi (varsayilan ~1.4x bosluk
  metni hapin disina tasiriyordu). Bes dilin hepsi bolundu.
- **Harita: hedef puntosu 25 -> 23 referans birimi** (kullanici: "hedeflerin
  yazilari 2px azalsin" — cihaz olceginde ~2px'e denk geliyor).
- **Sonuc diyalogu: skor/hedef paneli alcaldi (~24dp).** Kullanici cihazda
  "HARITAYA DON" butonunun yarisinin kesildigini gosterdi. Kok neden: diyalog
  Column'u 360x740dp ekranda (banner + durum/gezinme cubugu dusulunce ~590dp)
  ekrandan uzundu ve Compose kalan yeri SON cocuga veriyor — buton 54dp yerine
  ~37dp olculup yazisi ortadan kirpiliyordu. Panel dolgusu 12 -> 8dp, etiket
  12 -> 11sp, rakam 30 -> 26sp, ayirici 38 -> 30dp; hepsine lineHeight
  kelepcesi kondu (`ResultDialogKit.ResultStatPanel`).

### Testing
- `ResultDialogRenderTest.Backdrop` artik olcu parametresi aliyor ve yeni
  `renderLevelFailedNarrow` testi kullanicinin cihaz olcusunde (360x590dp)
  render aliyor — 411x760dp'lik eski arka plan bu hatayi hic uretemiyordu.

### Evidence
- `gradlew :app:assembleDebug` BUILD SUCCESSFUL -> `app-debug.apk` (28.0 MB)
- `gradlew :app:testDebugUnitTest --tests "*ResultDialogRenderTest*"`
  BUILD SUCCESSFUL -> `build/dialog-renders/level_failed_narrow.png`
  (uc buton da tam gorunuyor)
- Cihaz (SM-G965, 1080x2220 @480dpi): APK kuruldu, Kariyer haritasi ekran
  goruntusu alindi — 1. seviye hapi solda, dugumle ayni hizada, metin iki
  satir.
- Teslim: `builds/apk/KaboomBlocks_debug_map_pill_dialog_20260830.apk`

## [Unreleased] - Faz 162 (Gorsel tur: zemin/kart doygunlugu, 3B ucusan parcalar, neon kenarlik)

### Changed
- **MADDE 1 — Zemin gradyani doyuruldu.** Cihaz ile hedef mockup arasindaki
  fark TON degil DOYGUNLUK'tu: olculen zemin `#253F6D` (uc kanal birbirine
  yakin = tanim geregi gri), hedef `#01267C` (kirmizi kanal ~0).
  - Yeni `saturateDeep()` (`ui/theme/GameSurfaces.kt`): rengin GRI (akromatik)
    bilesenini soker — `min(r,g,b) * amount` her kanaldan cikarilir. Hue ve en
    guclu kanalin parlakligi korunur, yani renk kararmaz, kirlilikten arinir.
  - `gameSurfaces()` turetmesi "doyur -> accent tint -> TEKRAR doyur" sirasina
    gecti. Ikinci gecis, Oklab lerp'inin tint ile birlikte getirdigi gri payi
    soker. Accent tint'i bu sayede 0.30 -> 0.56'ya CIKARILABILDI (hedefin
    parlakligi ancak boyle yakalaniyor).
  - Olculen sonuc (SM-G950F): ust zemin `#253F6D` (sat %66) -> `#0E2A63`
    (sat %86); ic istatistik paneli `#080D18` (sat %67) -> `#0B1A33` (sat %78).
- **MADDE 2 — Kart govdesi doyuruldu ve koyulastirildi.** `panel`, `bandEven`,
  `bandOdd` ve `sunken` ayni `purify` katmanindan geciyor; `sunken` artik ham
  `bg` yerine doyurulmus `deepBg`den turuyor ve siyaha 0.45 yerine 0.10
  gidiyor (hedefteki lacivert korunuyor, komurlesmiyor).
  - `NeonCard` tonlama katsayilari dusuruldu (0.24 -> 0.16, 0.08 -> 0.05):
    kart govdesi `#163C58` -> `#122B46`, DESIGN_SPEC'in `#0E1835` degerine
    yaklasti ve camgobegi kaymasi kalkti. Mod kimligi kenarlik / dis hale /
    madalyon / baslikta tasindigi icin kaybolmuyor.
- **MADDE 3 — Ucusan parcalar artik 3B kup varligi.**
  `ui/common/WanderingPiecesBackground.kt` duz `drawRoundRect` yerine
  `R.drawable.kb_block_*` varliklarini ciziyor.
  - ⚠️ HAREKET DAVRANISI DEGISMEDI: `fx/fy/seed/speed` ve salinim formulu
    Faz 122'den beri aynen duruyor. Degisen yalnizca cizilen sey.
  - ⚠️ `ColorFilter.tint` UYGULANMIYOR — varliklar kendi golgesini tasiyor.
    `WanderingPiece.color` alani kalkti, yerine `asset` geldi.
  - Katman dokunma yakalamiyor (Canvas'ta `clickable`/`pointerInput` yok).
  - Varliklar `distinct()` ile tekillestirilip bir kez cozuluyor.
- **MADDE 4 — Neon kenarlik gucu parametrik.** `gameOuterGlow` artik
  `spreadStepDp` ve `coreAlpha` aliyor; VARSAYILANLAR ESKI GORUNUMU BIREBIR
  KORUR (3 katman / 2.2dp / 0.30), yani mod secim kartlari degismedi.
  - `NeonCard(bloom = ...)` eklendi (varsayilan `1f` = eski davranis);
    `bloom > 1` genis hale + ic kontur ("cift cizgi") getiriyor.
  - Onboarding/dil secim karti (`ui/onboarding/OnboardingScreen.kt`) artik
    14 katman x 1.3dp hale + parlak dis kenarlik + ic kontur kullaniyor.
    Ilk denemede 7 x 2.6dp verilmisti ve CIHAZDA ayri ayri halkalar olarak
    okundu (konturlar arasi ~7.8px, kalinlik 6px); aralik daraltilinca
    konturlar ortustu ve surekli bir hale olustu.
- **Ust serit parca yaricapi 22dp -> 16dp** (`ui/modeselect/ModeSelectScreen.kt`).
  Kup varliklari daha genis cizildigi icin en dar desteklenen ekranda (292dp)
  4'lu parcanin SAG KENARI x=0.194'e ulasiyor, yani `HEADER_TEXT_LEFT` (0.18)
  sinirini asip alt baslik sutununa giriyordu. Pim konumu (0.045) KORUNDU —
  0.02'ye cekmek denendi ve cihazda geri alindi (parcalar ekran disina kayip
  kenarda "dilim" olarak okunuyordu).

### Fixed
- `ModeSelectLayoutTest` ust serit tasma testi artik parcanin GERCEK CIZIM
  GENISLIGINI de hesaba katiyor. Onceden yalnizca MERKEZ kontrol ediliyordu;
  varliklar buyudugunde merkez sinirda kalirken kenar metin sutununa
  girebiliyordu — yani test yesil kalirken hata cihazda gorunur olacakti.

### Notes
- 94 test yesil, `lintDebug` temiz, `assembleDebug` yesil.
- 6 skin guvencesi: `GameSurfacesSkinTest` 12 kombinasyonu tariyor ve gecti.
  `purify` bant tonlarinin parlaklik farkini daraltiyordu; SUNSET (0.0036) ve
  PURPLE_NIGHT (0.0023) testin 0.004 esiginin ALTINA dusmustu — `bandOdd`
  artik doyurmadan SONRA sabit bir aciklastirma aliyor, en dusuk fark 0.0121.
- ACIK TEMA DEGISMEDI: `purify` acik yuzeylerde girdiyi oldugu gibi dondurur
  ve `sunken`in acik dali eski ifadeyle ayni. DEFAULT-light turetmesi iki
  olcumde de birebir ayni degerleri verdi (`skyTop #EAEFFB`, `panel #F1F4F8`,
  `sunken #B2B3B5`). Cihazda da dogrulandi.
- Cihazda dogrulandi (SM-G950F): mod secim, ayarlar, dil secim + onboarding,
  acik tema, Gun Batimi skini.
- Oynanis, skor, denge, reklam ve `versionCode` DEGISMEDI.

## [Unreleased] - Faz 161 (Ayarlar: ses siddeti satiri)

### Changed
- **Ses Siddeti satiri tek satira indi.** Onceki hali uc katliydi:
  (1) ikon + etiket + yuzde kapsulu, (2) tam genislik kaydirici,
  (3) altinda `0%` / `50%` / `100%` kademe etiketleri. Kart komsu ayar
  satirlarinin ~3 kati yuksekligindeydi ve ayni deger AYNI ANDA iki yerde
  yaziyordu (kapsul + ortadaki kademe etiketi).
  - Yeni `SettingsSliderRow` (`ui/settings/SettingsScreen.kt`): ikon + etiket +
    kaydirici + kapsul tek satirda. Etiket OLCULEN sabit genisligini alir,
    `weight(1f)` KAYDIRICIDA — tersi yapilirsa uzun ceviri satiri yer ve
    kaydiriciya 0dp kalir (Faz 159'da Loadout'ta yasanan hata).
  - Sigmazsa sikistirma yok: etiket + `SLIDER_MIN_TRACK` (76dp) + kapsul
    satira sigmiyorsa alt alta duzene duser. `SLIDER_MIN_TRACK` cihazda
    olcularak secildi (SM-G950F / 360dp: satir butcesi 308dp).
  - Kademe etiketleri kaldirildi (`GameSlider(showTicks = false)`); deger
    yalnizca sagdaki kapsulde. `GameSlider.showTicks` VARSAYILANI `true`,
    yani bilesenin baska kullanimlari degismiyor.
  - Kapsul her zaman `"100%"` genisligine gore olculuyor; boylece surukleme
    sirasinda kaydiricinin genisligi oynamiyor.
  - Kaydiricinin dokunma hedefi 48dp olarak korundu.

### Added
- `ui/settings/SettingsSliderRowTest.kt` (3 test): en uzun TR/EN/IT/FR/ES
  cevirilerinde kaydiricinin kullanilamaz genislige dusmemesi ve 48dp dokunma
  hedefi; degerin ekranda TAM BIR kez yazilmasi; `GameSlider`in kademe
  etiketlerini varsayilan olarak gostermeye devam etmesi.

### Notes
- Cihazda olculen degerler (SM-G950F, 360dp, satir butcesi 308dp):
  TR `Ses Siddeti` 275dp, IT `Volume Suoni` 292dp, FR `Volume Sonore` 302dp —
  ucu de TEK SATIR. ES `Volumen de Sonido` 331dp — sigmiyor, alt alta duzene
  duser (iki satir; eskisi gibi uc degil).

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
