# Boom Blocks Changelog

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
