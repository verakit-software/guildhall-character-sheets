# Phase One: Character Creation Tool — Breakdown

**Project:** Compose Multiplatform Character Creator (Android, iOS, Desktop)
**Approach:** Mobile-first; desktop as a stretch target
**Phase scope:** Basic character creation — user inputs values, views the resulting character on a separate screen
**Team:** 2–3 developers, parallel tracks where possible
**Game system:** D&D 5e / Pathfinder style (TTRPG)

---

## Assumptions

- TTRPG model fields (ability scores, proficiency bonus, hit dice, ASIs, saving throws, etc.) — adjust if you're closer to Pathfinder 2e (different concepts: ancestry/heritage instead of race, more granular feat tracks).
- You're **consuming a read-only API** for reference data (classes, feats, items, backgrounds, abilities). The user's actual character is persisted **locally only** for phase one.
- "Basic" in phase one means: enter values → see them rendered. **No** dice rolling, **no** levelling logic, **no** combat math, **no** backend sync.
- Stories are sized for **2–3 day chunks** typical of a multi-dev team. Dependencies are called out so you can stage them on the board.

---

## Architecture at a glance

```
:composeApp
  ├── commonMain        ← shared UI (Compose Multiplatform) + ViewModels
  ├── androidMain       ← Android entry point
  ├── iosMain           ← iOS entry point
  ├── desktopMain       ← Desktop entry point
:shared
  ├── commonMain
  │   ├── model         ← Character, CharacterClass, Item, Ability, Skill, Feat, Background
  │   ├── api           ← Ktor client, DTOs, mappers
  │   ├── repository    ← interfaces + impls
  │   └── persistence   ← local save/load (multiplatform-settings)
  ├── androidMain / iosMain / desktopMain  ← platform expect/actual
```

**Key libraries to settle on early (decide in Sprint 0):**
- Ktor client (multiplatform HTTP)
- kotlinx.serialization (JSON)
- kotlinx.coroutines + Flow
- Koin or Kotlin-Inject for DI
- Multiplatform-Settings for phase one persistence
- Voyager or Decompose for navigation (or Compose Navigation 2.8+ KMP)

---

## Suggested team split

With 2–3 devs, there are two natural parallel tracks once foundations are in:

- **Track A — Data:** Domain models → DTOs/mappers → repositories → persistence
- **Track B — UI:** Navigation shell → creation form → view screen
- **Track C — Platform polish (if you have a third dev):** iOS-specific UX, desktop window/menu handling, theming, accessibility

Track A and Track B can run in parallel after Sprint 1 if model interfaces are agreed up front. Track B uses **fake repositories** from story 3.3 while Track A finishes the real ones.

---

## Epic structure

1. **Project Foundation** — repo, build, multi-target scaffold, CI
2. **Domain Models** — the data classes you listed
3. **API Layer** — Ktor client, DTOs, mappers, repository
4. **Character Creation Flow** — input screen, validation, state
5. **Character View Flow** — render screen, navigation, local persistence

---

## Epic 1 — Project Foundation

**Dependencies:** none. Everything else blocks on this.

### STORY 1.1 — Bootstrap Compose Multiplatform project
**As a** developer **I want** a working KMP project targeting Android, iOS, and Desktop **so that** the team has a baseline to build on.

**Acceptance criteria:**
- Project created from the JetBrains Kotlin Multiplatform Wizard
- `./gradlew :composeApp:assembleDebug` succeeds (Android)
- iOS app builds and launches in the simulator
- Desktop app launches via `./gradlew :composeApp:run`
- "Hello, world" Composable renders on all three targets
- README has run instructions per target

**Notes:** Decide and lock in `gradle/libs.versions.toml`: min Android SDK (24+), iOS deployment target (15+), JVM target (17). Get this right once — changing later is painful.

**Estimate:** 1 day · **Owner:** one dev pairs through it so everyone learns the toolchain

---

### STORY 1.2 — Shared module + version catalog
**As a** developer **I want** a `:shared` module with version catalog **so that** model/api/repository code lives separately from UI.

**Acceptance criteria:**
- `:shared` module with `commonMain` + platform source sets
- `libs.versions.toml` lists Ktor, kotlinx-serialization, coroutines, chosen DI lib
- `:composeApp` depends on `:shared` and successfully calls a shared function from common code

**Estimate:** 0.5 day · **Blocks:** Epic 2, Epic 3

---

### STORY 1.3 — CI pipeline (smoke)
**As a** team **we want** CI that builds all three targets on PR **so that** we catch platform-specific breakage early.

**Acceptance criteria:**
- GitHub Actions / GitLab CI builds Android + Desktop on push
- iOS build runs on a macOS runner (or noted as known-skip if no macOS runner available)
- Status check required for merging to main
- Lint runs and fails the build on errors

**Estimate:** 1 day · **Can run in parallel with 1.4, 1.5**

---

### STORY 1.4 — Pick and wire navigation
**As a** developer **I want** a navigation solution that works on all three platforms **so that** I can move between create and view screens.

**Acceptance criteria:**
- Navigation library chosen and documented (Voyager or Decompose recommended; Compose Navigation 2.8+ also works on KMP)
- Two placeholder screens registered: `CharacterCreateScreen`, `CharacterViewScreen`
- Button on Create navigates to View; back navigation works
- Verified on Android, iOS, Desktop

**Estimate:** 1 day · **Blocks:** Epic 4, Epic 5

---

### STORY 1.5 — Wire DI container
**As a** developer **I want** Koin (or Kotlin-Inject) configured in common code **so that** I can inject repositories and ViewModels.

**Acceptance criteria:**
- DI graph defined in `commonMain`
- Platform-specific init called from Android, iOS, Desktop entry points
- A trivial test object resolves correctly from common code on all targets

**Estimate:** 0.5 day · **Can run in parallel with 1.4**

---

### STORY 1.6 — Design system foundations
**As a** developer **I want** a shared theme, typography, and color tokens **so that** screens look consistent and dark mode works out of the box.

**Acceptance criteria:**
- Material 3 theme set up in `commonMain` with light + dark variants
- Color tokens, typography scale, and spacing tokens defined
- Sample screen demonstrates theme on all three targets

**Estimate:** 1 day · **Can run in parallel with 1.3–1.5** · **Good Track C starter**

---

## Epic 2 — Domain Models

**Dependencies:** Stories 1.1, 1.2 done. Most of this epic can be parallelized — each model is independent until story 2.8 (the aggregate).

All models live in `:shared/commonMain/model`. Use `@Serializable` data classes throughout.

### STORY 2.1 — Ability scores and modifiers
**Model:** Rename the stat enum to `AbilityScoreType` (or `Attribute`) to avoid clashing with the game-power `Ability` model in 2.7. Then `AbilityScore(type: AbilityScoreType, value: Int)` with a derived `modifier` property.

**Acceptance:** unit tests covering edge values (score 1 → mod -5; score 10 → mod 0; score 20 → mod +5; score 30 → mod +10).

**Estimate:** 0.5 day

---

### STORY 2.2 — Skill model
**Model:** `Skill(name: String, governingAbility: AbilityScoreType, isProficient: Boolean, expertise: Boolean = false)`. Include an enum or sealed list of the standard 18 skills.

**Acceptance:** round-trip JSON serialization; proficient/expert flags survive encoding.

**Estimate:** 0.5 day · **Parallel with 2.1**

---

### STORY 2.3 — Feat and Background models
**Models:**
- `Feat(id: String, name: String, description: String, prerequisites: List<String>)`
- `Background(id: String, name: String, description: String, grantedSkills: List<String>, grantedFeats: List<String>)`

**Acceptance:** sample JSON fixtures parse without errors; both serialize cleanly.

**Estimate:** 1 day · **Parallel with 2.1, 2.2**

---

### STORY 2.4 — CharacterClass model with ASIs
**Model:** `CharacterClass(id, name, hitDie: Int, primaryAbility: AbilityScoreType, savingThrows: List<AbilityScoreType>, abilityScoreImprovements: List<AbilityScoreImprovement>)`. `AbilityScoreImprovement(level: Int, ability: AbilityScoreType, bonus: Int)`.

**Acceptance:** sample "Fighter" fixture with ASI entries at levels 4, 8, 12, 16, 19; round-trip serialization passes.

**Estimate:** 1 day · **Parallel with 2.1–2.3**

---

### STORY 2.5 — Item and ItemStats models
**Models:**
- `ItemStats(damage: String?, armorClass: Int?, weight: Double, properties: List<String>)` — nullable fields cover weapons/armor/misc
- `Item(id, name, description, stats: ItemStats, isCustom: Boolean = false)`

**Acceptance:** longsword, plate armor, and a misc trinket all fit the shape; round-trip passes.

**Estimate:** 1 day · **Parallel with 2.1–2.4**

---

### STORY 2.6 — Custom Item storage hook
**As a** user **I want** to define my own items **so that** homebrew gear is supported.

Same `Item` shape with `isCustom = true`. Story captures the **storage path** — custom items don't come from the API and need a clear repo path.

**Acceptance:** repository interface (story 3.3) has a clear hook for custom items vs. API items; documented in code.

**Estimate:** 0.5 day · **Depends on 2.5**

---

### STORY 2.7 — Character ability/power model
**Model:** `CharacterAbility(id, name, description, source: AbilitySource, usesPerRest: Int?, rechargeOn: RestType?)` where `AbilitySource` is `enum { CLASS, RACE, FEAT, ITEM }`.

**Acceptance:** "Second Wind" and "Action Surge" fixtures build cleanly; round-trip passes.

**Estimate:** 1 day · **Parallel with 2.1–2.5**

---

### STORY 2.8 — Character aggregate
**As a** developer **I want** a top-level `Character` model that composes everything **so that** I have a single object to pass between screens.

```kotlin
@Serializable
data class Character(
    val id: String,
    val name: String,
    val level: Int,
    val characterClass: CharacterClass,
    val background: Background,
    val abilityScores: List<AbilityScore>,
    val skills: List<Skill>,
    val feats: List<Feat>,
    val abilities: List<CharacterAbility>,
    val inventory: List<Item>,
    val notes: String = ""
)
```

**Acceptance:** complete fixture builds; full round-trip JSON serialization passes; unit tests verify derived getters (e.g., proficiency bonus from level).

**Estimate:** 1 day · **Depends on 2.1–2.7**

---

## Epic 3 — API Layer

**Dependencies:** Epic 2 models exist (at least their interfaces). Track A's main focus.

### STORY 3.1 — Ktor client setup
Configure Ktor in common code with platform engines (OkHttp on Android, Darwin on iOS, CIO on Desktop). JSON content negotiation, logging, base URL config from BuildConfig/env.

**Acceptance:** smoke-test endpoint returns 200 and parses on all three platforms; auth header strategy (if any) defined.

**Estimate:** 1 day · **Blocks:** 3.2, 3.4

---

### STORY 3.2 — DTOs and mappers
For each domain entity sourced from the API, create a DTO under `api/dto/` and a mapper to the domain type. DTOs are independent from domain so API churn doesn't ripple into the UI.

**Acceptance:** mapper unit tests for class, item, feat, skill, background, ability.

**Estimate:** 2 days (can split: one dev does class/feat/background, another does item/ability/skill)

---

### STORY 3.3 — Repository interfaces + fakes
Define in `commonMain`:
- `CharacterClassRepository`, `FeatRepository`, `BackgroundRepository`, `ItemRepository`, `AbilityRepository`, `SkillRepository`
- `CharacterRepository` (local-only for phase one)

Each returns `Flow<List<T>>` or suspend functions returning `Result<T>`. Provide **fake implementations** with hard-coded data for use in previews, tests, and to unblock Track B.

**Acceptance:** interfaces compile; fakes return realistic sample data; Track B can build screens against fakes.

**Estimate:** 1 day · **Prioritize this — it unblocks Track B**

---

### STORY 3.4 — Repository implementations
Real impls that call Ktor and map DTOs to domain. In-memory caching so repeated reads don't refetch.

**Acceptance:** integration test (or manual smoke) shows each repo returning real data from the API on at least one platform; error paths return `Result.failure` with typed exceptions.

**Estimate:** 2 days · **Depends on 3.1, 3.2, 3.3**

---

### STORY 3.5 — Local persistence for Character
Use **multiplatform-settings** — sufficient for phase one (single character or small list as JSON blobs).

**Acceptance:** create a character, kill the app, relaunch — character is still there. Works on all three platforms; concurrent-write safety isn't a concern in phase one but document it.

**Estimate:** 1 day · **Depends on 2.8**

---

## Epic 4 — Character Creation Flow

**Dependencies:** Epic 1 done, story 3.3 done (so Track B has fakes to work against). Real API not required until integration.

### STORY 4.1 — CreateCharacterViewModel + state
Multiplatform ViewModel (moko-mvvm, Decompose component, or a hand-rolled state holder backed by a `StateFlow`). Exposes a `CharacterCreationState` and intent handlers.

**Acceptance:** state updates correctly per intent; unit tests for the reducer; works with both fake and real repositories.

**Estimate:** 1 day · **Blocks:** all other Epic 4 stories

---

### STORY 4.2 — Name, level, notes inputs
Simplest fields. Validates name non-empty, level 1–20.

**Acceptance:** mobile-first layout; inline field-level validation; tested on Android emulator + iOS sim.

**Estimate:** 1 day

---

### STORY 4.3 — Ability score inputs
Six number inputs (STR through CHA). Show derived modifier live next to each. Constrain 1–30 with clamping or validation.

**Acceptance:** live modifier updates; out-of-range values flagged or clamped consistently.

**Estimate:** 1 day · **Parallel with 4.2**

---

### STORY 4.4 — Class picker
Bottom sheet on mobile, dropdown/dialog on desktop. Populated from `CharacterClassRepository`. Loading + error states.

**Acceptance:** picker loads (from fake or real repo); selection updates state; error state shown if load fails. Establish the **picker pattern** here — it'll be reused in 4.5 and 4.7.

**Estimate:** 1.5 days · **Parallel with 4.2, 4.3** · **Pattern reused later**

---

### STORY 4.5 — Background picker
Same pattern as 4.4 against `BackgroundRepository`.

**Acceptance:** mirrors 4.4.

**Estimate:** 0.5 day · **Depends on 4.4** (pattern is settled)

---

### STORY 4.6 — Skills selection
Checkbox list with proficiency toggles. Optionally show governing ability next to each name.

**Acceptance:** mark/unmark proficiency; state reflects selection; scannable on a phone screen.

**Estimate:** 1 day · **Parallel with 4.4, 4.5**

---

### STORY 4.7 — Feats selection
Multi-select list from `FeatRepository`. Tap a feat to see description in detail view or expanding card.

**Acceptance:** add/remove feats; detail view readable on small screens.

**Estimate:** 1.5 days · **Depends on 4.4** (reuses picker pattern)

---

### STORY 4.8 — Inventory: API items + custom items
Two paths in one screen: pick from API items, OR add a custom one via a small form (name, description, stats fields). Custom items get `isCustom = true`.

**Acceptance:** mix API items and custom items in a single character's inventory; custom items round-trip through persistence.

**Estimate:** 2 days · **Depends on 4.4** (pattern) **and 3.3** (fake repo)

---

### STORY 4.9 — Submit / save
"Create" button validates the whole form, builds the `Character`, hands it to `CharacterRepository.save()`, navigates to view screen.

**Acceptance:** invalid form shows validation summary; valid form persists and navigates; works on all platforms.

**Estimate:** 1 day · **Depends on 3.5, 4.1–4.8**

---

## Epic 5 — Character View Flow

**Dependencies:** Epic 1 done, 3.3 fakes available. Can start in parallel with late Epic 4 stories.

### STORY 5.1 — ViewCharacterViewModel
Loads the saved character from `CharacterRepository`. Exposes read-only state.

**Acceptance:** loads correctly; empty/error/loading states handled.

**Estimate:** 0.5 day · **Blocks other Epic 5 stories**

---

### STORY 5.2 — Header section
Name, class, level, background. Mobile-first: stacked layout; desktop can go side-by-side.

**Acceptance:** renders correctly across all three targets at representative window sizes.

**Estimate:** 1 day

---

### STORY 5.3 — Ability scores + skills display
Grid of ability scores with modifiers prominent. Skills grouped by proficiency status.

**Acceptance:** information-dense but scannable on a 5" phone; tested in light + dark.

**Estimate:** 1 day · **Parallel with 5.2**

---

### STORY 5.4 — Feats and abilities display
Collapsible cards or a tabbed section. Each shows name + description; abilities also show source and uses-per-rest.

**Acceptance:** long descriptions don't overflow; expand/collapse smooth on mobile.

**Estimate:** 1.5 days · **Parallel with 5.2, 5.3**

---

### STORY 5.5 — Inventory display
List of items grouped by type or alphabetical. Custom items flagged visually (e.g., a "homebrew" tag).

**Acceptance:** matches creation-screen data exactly; weight/AC/damage shown where present.

**Estimate:** 1 day · **Parallel with 5.2–5.4**

---

### STORY 5.6 — Edit / delete affordances
"Edit" button navigates back to creation screen pre-populated. "Delete" with confirmation dialog.

**Acceptance:** edit preserves all data; delete clears local storage and navigates to a sensible default.

**Estimate:** 1 day · **Depends on 5.1, 4.1**

---

## Suggested kanban columns

`Backlog` → `Ready` → `In Progress` → `In Review` → `Done`

Add a `Blocked` swimlane — API issues and platform-specific Compose bugs are likely the biggest external blockers.

Tag stories with **Track A (Data)** / **Track B (UI)** / **Track C (Platform)** labels so it's visually clear what runs in parallel.

## Suggested sprint shape (2-week sprints, 2–3 devs)

### Sprint 0 (1 week, optional spike)
All hands. Decide library choices (navigation, DI, persistence). Time-box research, document decisions in an ADR.

### Sprint 1 — Foundations
- All devs on Epic 1 (1.1 is best paired; 1.3–1.6 split across devs in parallel)
- Begin story 2.1 / 2.2 once 1.2 lands

### Sprint 2 — Models + API foundations
- **Track A:** finish Epic 2 (split 2.1–2.7 across two devs); start 3.1, 3.2
- **Track B:** start 3.3 (fakes), then begin 4.1, 4.2

### Sprint 3 — Repositories + creation form
- **Track A:** 3.4, 3.5 (real repos, persistence)
- **Track B:** 4.3–4.7 (picker pattern, skills, feats)
- **Track C (if 3rd dev):** 1.6 polish, accessibility, iOS-specific UX

### Sprint 4 — Creation finish + view screen
- **Track A:** integration testing, API edge cases
- **Track B:** 4.8, 4.9, then 5.1–5.4
- **Track C:** desktop window/menu polish, theming refinement

### Sprint 5 — View screen finish + cross-platform polish
- Finish 5.5, 5.6
- Bug bash across all three platforms
- Buffer for the inevitable iOS Compose layout surprises

## Risks to flag now

- **iOS Compose Multiplatform stability** for complex layouts — some advanced layout APIs are still alpha; budget buffer time in Sprints 4–5.
- **API contract instability** — the DTO/mapper split in story 3.2 is your insurance policy. Track A owns shielding the UI from API churn.
- **Naming collision** between `AbilityScoreType` (stat) and `CharacterAbility` (game power) — resolved in stories 2.1 / 2.7. Lock the naming early; renaming after Epic 2 is annoying.
- **Picker UX divergence** between mobile (bottom sheet) and desktop (dropdown/dialog) — pattern is settled in story 4.4 and reused. Don't let multiple devs each invent their own picker.
- **Parallel work merge friction** — agree on module/package conventions in Sprint 0 and stick to them. Repository interfaces (3.3) are the explicit contract between Tracks A and B; treat them as a stable handshake.
- **Fake-to-real repository swap** — Track B builds against fakes, so make sure the real impls (3.4) honor the same `Flow`/`Result` semantics or the swap-in will hide bugs until late.
