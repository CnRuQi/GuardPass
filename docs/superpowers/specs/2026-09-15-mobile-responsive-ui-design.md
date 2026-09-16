# Mobile-First Responsive UI Design

## Goal

Make the password manager usable at common phone widths without clipping, double insets, navigation overlap, or fixed-height dead space, while retaining the existing data flows and visual identity.

## Evidence And References

- Android's adaptive Compose guidance recommends window size classes and adaptive navigation rather than separate hard-coded phone/tablet layouts: https://developer.android.com/develop/ui/compose/layouts/adaptive/get-started-with-adaptive-apps?hl=zh-cn
- Android's adaptive navigation guidance recommends a bottom navigation bar for compact windows and a side navigation rail for expanded windows, with runtime switching: https://developer.android.com/develop/ui/compose/layouts/adaptive/build-adaptive-navigation?hl=zh-cn
- The current implementation mixes `Scaffold` padding, `statusBarsPadding()`, system bar padding values, and fixed bottom padding across screens. `VaultScreen`, `CategoryScreenV2`, `SettingsScreenV2`, and `SearchScreenV2` therefore do not share one inset contract.

## Design Decisions

### 1. One adaptive app shell

`AppNavHost` remains responsible for routing. `AppScaffold` becomes the only owner of root navigation and root safe-area padding for top-level destinations. Compact widths keep a Material 3 `NavigationBar`; medium and expanded widths use a `NavigationRail`. The route list and selected-route behavior remain unchanged.

The shell will use the existing `BoxWithConstraints` approach for this pass so the change does not require a new adaptive dependency or a broad Compose version upgrade. The breakpoint is centralized in one place and is based on available width, not screen density or device name.

### 2. One inset contract

Every top-level screen receives the `PaddingValues` produced by the shell and consumes it exactly once at its root. Screens must not add a second `statusBarsPadding()` or a fixed bottom spacer to compensate for navigation. Standalone routes such as search and entry editing own a local `Scaffold`/inset contract and use `imePadding()` for the keyboard.

The app continues to use edge-to-edge. System bars, the bottom navigation surface, and the IME are treated as layout inputs; they are not simulated with arbitrary `dp` values.

### 3. Phone-first content geometry

- Compact content uses 16.dp horizontal gutters, with 20.dp only where the existing visual hierarchy benefits from it.
- Lists and forms remain full-width within those gutters and may wrap or ellipsize text instead of forcing horizontal overflow.
- Expanded content gets a readable max width while the navigation rail owns the left edge.
- FABs sit above the shell's bottom content area and no longer depend on a fixed `bottom = 96.dp` list padding.
- Touch targets remain at least 48.dp even when icon glyphs are smaller.

### 4. Screen responsibilities

- Vault and Favorites: one scrollable content region, filter row stays horizontally scrollable, list bottom padding comes from the shell contract.
- Categories: list and empty state share the same content geometry; the FAB remains visible above the navigation bar.
- Settings: section cards and form fields use the same gutters and wrap action rows on narrow widths.
- Search: the top app bar, query field, and result list share a standalone safe-area contract; keyboard appearance must not hide the query field or last result.
- Entry editor: form fields remain in one vertical scroll region and the save action remains reachable above the IME and navigation bar.

### 5. Validation

The existing instrumentation tests remain the behavioral baseline. Add focused Compose UI assertions for compact layouts at approximately 360x800 and 412x915: the root renders, navigation items are reachable, the first list item is inside the viewport, and no critical action is clipped. Run debug build, unit tests, and instrumentation tests when an emulator is available.

## Non-Goals

- No database, repository, encryption, backup, or OAuth changes.
- No visual theme reset or brand redesign in this pass.
- No tablet-specific list-detail flow until the phone layout is stable.
