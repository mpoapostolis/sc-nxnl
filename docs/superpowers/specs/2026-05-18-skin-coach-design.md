# Skin Coach — Design Spec

**Date:** 2026-05-18
**Status:** Approved — moving to implementation planning
**Working name:** "Skin Coach" (final name TBD)

## 1. Concept

A native Android app. The user takes a selfie; the app analyzes their skin on-device and
returns a **Skin Score (0–100)** with a per-concern breakdown (acne, wrinkles/fine lines,
pores, dark spots/hyperpigmentation, redness, oiliness). It then generates a **personalized
AM/PM skincare routine** based on the detected concerns. The user re-scans weekly and tracks
**progress over time** (score history, before/after photos).

## 2. Goal

A money-making app for a solo developer. Realistic target: $1–10k/month. Strategy: a viral,
single-purpose camera app (proven pattern — Cal AI, Umax) combined with a subscription, in
the large and growing skin-analysis market.

## 3. Core User Flow

1. Onboarding — brief: what the app does, camera permission, optional skin-goal questions.
2. Guided selfie capture — framing + lighting guidance.
3. On-device skin analysis → Skin Score + per-concern breakdown.
4. Personalized routine — AM/PM steps + ingredient suggestions.
5. Weekly re-scan → progress tracking (score trend, photo timeline).
6. Paywall gating the paid value.

## 4. Monetization

**Freemium. Gate the value, not the scan count.**

- **Free, unlimited:** scan + Skin Score + basic top-level read. This is the viral engine —
  every scan is a shareable result; scans cost $0 to serve (on-device).
- **Subscription (paid):** full personalized routine, progress tracking + history, detailed
  per-concern analysis, product recommendations.
- **Trial:** 3–7 days.
- **Pricing:** annual $34.99 (default), weekly $5.99 (impulse), lifetime ~$79.
- **Billing:** Google Play Billing via RevenueCat.
- **#1 post-launch A/B test:** paywall placement. Hard paywalls convert ~5× better in general
  but suppress the free viral action — keep paywall placement configurable.

## 5. MVP / v1 Scope

**In:** onboarding, guided capture, on-device analysis + Skin Score, routine generator,
progress tracking + history, paywall + trial.
**Out (deferred):** sharing features beyond the OS share sheet, community, in-app product
shop/affiliate, iOS, cloud sync/accounts.

## 6. Architecture

- Android native — Kotlin, Jetpack Compose, single-Activity.
- Pattern: MVVM. Layers — **UI** (Compose screens + viewmodels) / **domain** (analysis
  orchestration, scoring, routine logic) / **data** (Room, analyzer, billing).
- **CameraX** for capture.
- **Room** for local persistence (scan history, routines, settings). Photos in app-local storage.
- **RevenueCat SDK** for subscription/paywall/entitlements.
- Skin analysis sits behind a clean **`SkinAnalyzer` interface** — `analyze(image) →
  SkinAnalysis`. Implementations are swappable (on-device model / cloud). v1 targets on-device.

## 7. Skin Analysis Component (primary risk)

- **Contract:** input a face image → output a structured `SkinAnalysis` (per-concern scores +
  overall Skin Score + detected concern flags).
- **Pipeline:** face detection / landmarks (ML Kit / MediaPipe, on-device) → skin-region crop
  → skin-attribute model → scores.
- **Model sourcing options** (decided via a Phase-2 spike): (a) licensed skin-analysis SDK,
  (b) own model trained on open skin datasets, (c) cloud model. On-device preferred (zero
  marginal cost, privacy).
- **De-risk early:** the first milestone after the skeleton is "produce a believable,
  consistent score." The swappable interface means a cloud fallback never forces an app rewrite.

## 8. Skin Score & Routine Logic

- **Skin Score:** 0–100 composite from weighted per-concern sub-scores. Deterministic, testable.
- **Routine generator:** rule-based — maps detected concerns to AM/PM steps + ingredient
  suggestions (e.g. acne → salicylic acid; dark spots → vitamin C + SPF). No ML required.
  Deterministic, testable.

## 9. Data Model (local, Room)

- `ScanResult`: id, timestamp, photo path, overall score, per-concern scores.
- `Routine`: derived from a scan; AM/PM steps.
- `AppSettings` / subscription state (entitlement cached; RevenueCat is source of truth).

## 10. Privacy

Face photos are sensitive personal data. Default posture: **all processing and storage
on-device**, no upload. Clear privacy policy (hosted free, e.g. GitHub Pages) — required by
Play. On-device processing is also a marketing point. If a cloud analyzer is ever used,
explicit user consent is required first.

## 11. UI / Design Quality

A first-class requirement. The UI must be **polished, fast, distinctive** — not a generic
AI-app look. Cohesive design system (color, type, motion). Smooth camera flow and result
reveal. The result/score screen must be visually striking and **shareable** (the viral hook).

## 12. Build Phases

1. **Foundation** — project scaffold, Gradle, single-Activity Compose, navigation, design
   system/theme, camera capture screen.
2. **Skin analysis** — `SkinAnalyzer` interface + first implementation, Skin Score, result
   screen. *De-risk milestone.*
3. **Routine + progress** — routine generator, progress tracking, history/timeline.
4. **Monetization** — RevenueCat paywall, trial, onboarding polish.
5. **Launch prep** — store listing, app icon, privacy policy, polish pass.

## 13. Testing

- Unit tests (TDD where practical): Skin Score computation, routine generator — pure
  deterministic logic.
- `SkinAnalyzer`: contract tested with fake implementations; real model evaluated against
  sample images.
- UI: Compose previews + manual device testing.

## 14. Risks

- **Skin model quality** — mitigated by the swappable interface + an early de-risk spike.
- **Distribution** — make-or-break for any viral app; mitigated by the shareable-score loop
  and an organic launch (TikTok/Reels). Outside the build itself but noted.
- **Android monetizes weaker than iOS** (~2× less spend) — known; an iOS port is a later option.

## 15. Open Decisions

- Skin model source — resolved in the Phase 2 spike.
- Final app name.
- Pricing fine-tuning post-launch.
