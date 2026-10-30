# Design

QRCache should feel **fast, calm and polished**, closer to a well-made Apple app than a typical utility scanner full of banners and buttons. This doc is the source of truth for how it looks and moves.

## Principles

1. **Instant.** The camera is the first thing you see. Every tap responds within one frame. No spinners for local actions.
2. **Quiet.** One primary action per screen. Generous white space. No ads, no clutter.
3. **Soft and physical.** Rounded corners, layered sheets, spring motion, light haptics. Things slide and settle, they don't just appear.
4. **Familiar on Android.** Apple-style polish, but Android behavior: system back gesture, Material components underneath, respects the user's font size and dark mode.

## How we get the Apple feel on Android

We build on Material 3 (for accessibility and Android behavior) and restyle it:

| Apple trait | How we do it |
|---|---|
| Large titles that shrink on scroll | `LargeTopAppBar` with exit-until-collapsed scroll behavior |
| Grouped, inset lists | Rounded cards with 12dp radius on a slightly tinted background, rows separated by hairlines |
| Bottom sheets with a grabber | `ModalBottomSheet`, 20dp top radius, visible drag handle |
| Spring animations | `spring(dampingRatio = 0.8f, stiffness = 380f)` as the default for movement |
| Frosted bars | Translucent tab bar and top bar over content (blur on Android 12+, solid translucent fallback below) |
| Subtle haptics | `HapticFeedbackType` light tick on scan success, swipe threshold, toggle |
| SF-like typography | Inter font, tight letter spacing on titles |

## Color

Neutral surfaces, one accent. Defined as theme tokens in `ui/theme/Color.kt`; never hard-code colors in screens.

| Token | Light | Dark | Use |
|---|---|---|---|
| `accent` | `#0A84FF` | `#409CFF` | Primary buttons, links, active tab |
| `success` | `#30D158` | `#32D74B` | Scan success flash |
| `danger` | `#FF3B30` | `#FF453A` | Delete |
| `background` | `#F2F2F7` | `#000000` | Screen background |
| `surface` | `#FFFFFF` | `#1C1C1E` | Cards, sheets, list groups |
| `surfaceSecondary` | `#E5E5EA` | `#2C2C2E` | Search field, chips |
| `textPrimary` | `#000000` | `#FFFFFF` | Titles, body |
| `textSecondary` | `#6C6C70` | `#AEAEB2` | Previews, timestamps |
| `separator` | `#C6C6C8` | `#38383A` | Hairlines between rows |

Dynamic color (Material You, Android 12+) is **off** by default so the app looks consistent everywhere; it can be added as a setting later.

## Typography

Font: **Inter** (bundled, open license). Sizes in `sp` so they scale with the user's font setting.

| Style | Size / line height | Weight | Use |
|---|---|---|---|
| Large title | 34 / 41 | Bold | Screen titles (expanded) |
| Title | 20 / 25 | Semibold | Sheet titles, collapsed bars |
| Headline | 17 / 22 | Semibold | Row titles |
| Body | 17 / 22 | Regular | Content |
| Subhead | 15 / 20 | Regular | Previews |
| Footnote | 13 / 18 | Regular | Timestamps, section headers |
| Caption | 12 / 16 | Medium | Chip labels, tab labels |

## Spacing and shape

- Base unit **4dp**. Common values: 4, 8, 12, 16, 20, 24, 32.
- Screen side padding: **16dp**.
- List row height: at least **56dp**; touch targets at least **48dp**.
- Corner radius: buttons and chips **10dp**, cards and list groups **12dp**, sheets **20dp**, viewfinder **24dp**.

## Motion

| Moment | Animation |
|---|---|
| Scan success | Viewfinder corners snap inward and flash `success` (150ms), haptic tick, sheet springs up |
| Sheet open/close | Spring, follows the finger when dragged |
| Tab switch | Crossfade, 180ms |
| Row delete | Row collapses with spring; Undo snackbar |
| List changes | `animateItem()` for reorders and inserts |
| Press | Scale to 0.97 while pressed |

Respect **Remove animations** in Android accessibility settings: fall back to fades with no movement.

Performance budget: 60fps on a 2016-era phone (Android 6/7), 120fps where the screen supports it. Use Baseline Profiles so the first launch is smooth.

## Icons

- Rounded, outlined style (Material Symbols Rounded, weight 400), filled when selected.
- Type icons: Link `link`, Text `notes`, Wi-Fi `wifi`, Contact `person`, Email `mail`, Phone `call`, SMS `sms`, Location `location_on`, Event `event`.
- App icon: a rounded square with a simplified QR corner marker and a small clock hand, accent blue on white. Adaptive icon with monochrome layer for Android 13+ themed icons.

## Components

| Component | Notes |
|---|---|
| `ScanViewfinder` | Rounded corner brackets only, no full box. Dimmed area outside. |
| `HistoryRow` | Icon in a 36dp tinted rounded square, title + preview, time on the right. |
| `HistoryGroup` | Section header (footnote, uppercase off) + rounded card of rows. |
| `SearchField` | Pill shape, `surfaceSecondary` fill, magnifier icon, clear button. |
| `FilterChip` | Pill, filled `accent` when selected. |
| `PrimaryButton` | Full width, 50dp tall, `accent` fill, 12dp radius. |
| `ActionRow` | Icon + label buttons in a row inside the detail sheet (Copy, Share, Rename, Delete). |
| `EmptyState` | Illustration, one line of text, one button. |

## Accessibility

- Every icon button has a content description.
- Contrast at least 4.5:1 for text (WCAG AA). `textSecondary` meets this on both backgrounds.
- Works with TalkBack: history rows read as "Title, type, time".
- Layout holds up at 200% font size.

## Screens (wireframes)

```
 Scan                        History                     Detail sheet
┌──────────────────┐        ┌──────────────────┐        ┌──────────────────┐
│             ⚡    │        │ History          │        │      ───         │
│                  │        │ ┌──────────────┐ │        │ 🔗 Link          │
│   ┌─      ─┐     │        │ │ 🔍 Search    │ │        │ Hotel breakfast  │
│                  │        │ └──────────────┘ │        │ menu        ✎    │
│   └─      ─┘     │        │ All Fav Links ...│        │ menu.hotel.com/… │
│                  │        │ TODAY            │        │ Today, 8:12 AM   │
│                  │        │ ┌──────────────┐ │        │ ┌──────────────┐ │
│                  │        │ │🔗 Breakfast  │ │        │ │  Open link   │ │
│   [🖼 From image] │        │ │   menu  8:12 │ │        │ └──────────────┘ │
│                  │        │ └──────────────┘ │        │ Copy Share ★  🗑 │
├──────────────────┤        ├──────────────────┤        └──────────────────┘
│ Scan History  ⚙  │        │ Scan History  ⚙  │
└──────────────────┘        └──────────────────┘
```
