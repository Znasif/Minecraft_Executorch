# AI Construction Copilot
## Qualcomm × Meta ExecuTorch Hackathon

### What This App Is
An offline AR construction assistant for Samsung S25 (Snapdragon 8 Elite).
Users point their phone at real objects. On-device AI identifies them and maps
them to virtual building materials — like Minecraft in the real world. AR
overlays a step-by-step build guide. No cloud. No internet. Everything runs
on the Hexagon NPU.

### The Two AI Models
YOLO mobile .pte — runs every frame, detects real objects in scene
Smol VLM .pte — runs on demand when user taps Verify Step,
reasons about whether the construction step looks complete

Both models are bundled in app/src/main/assets/
Both run via ExecuTorch on Snapdragon Hexagon NPU
Neither makes network calls

### ARCore vs AI — Hard Boundary
ARCore owns: floor detection, plane detection, spatial anchors,
measurements, blueprint overlay positioning
AI owns: object identification, step verification, reasoning
Never use AI for geometry. Never use ARCore for inference.

### Tech Stack
Android: Kotlin, Jetpack Compose, Coroutines, Flow
AR: ARCore (com.google.ar:core:1.41.0)
Camera: CameraX (camera-core/camera2/lifecycle/view 1.3.1)
Inference: ExecuTorch, QNN backend, Hexagon NPU
Architecture: single-module, MVVM, Compose Navigation

### Hard Constraints — NEVER BREAK
- NO internet permission in AndroidManifest.xml ever
- NO arsceneview wrapper library ever
- NO cloud inference ever
- NO hardcoded measurement values (use live ARCore data)
- CameraX owns the camera session always (PreviewView drives the preview)
- ARCore session created separately; feeds plane data via session.update() loop
- All .pte models loaded from assets, not downloaded

### Current State
- Live CameraX camera feed in EnvironmentScanScreen ✓
- ARCore plane detection running, dimensions wired to UI labels ✓
- Plane polygon projected to screen via VP matrices on Canvas overlay ✓
- Camera permission requested at runtime in EnvironmentScanScreen ✓
- Nav graph trimmed to 8 active screens ✓
- Next: wire YOLO inference to ARConstructionScreen object detection

### Active Screens (8)
SplashScreen        — 2.5s splash, auto-advances to HOME
HomeDashboardScreen — 2×2 card grid + 3-tab bottom nav
EnvironmentScanScreen — live CameraX preview + ARCore plane scan
BuildTimelineScreen — step list, accessible from HOME Continue
ARConstructionScreen — main AR build view, YOLO runs here
VerificationScreen  — VLM step verification
CompletionScreen    — build complete, stats
SettingsScreen      — app settings

### Deleted Screens (do not recreate)
ProjectCreationScreen, PlotSummaryScreen, FloorPlanSelectionScreen, AIAssistantScreen

### Navigation Flow
SPLASH → HOME (auto 2.5s)
HOME → SCAN         (Start New Project button)
HOME → TIMELINE     (Continue Project button)
HOME → AR           (Demo Mode button)
HOME → SETTINGS     (Settings nav tab)
HOME → SCAN         (Scanner nav tab)
SCAN → AR           (Finish Scan button)
AR   → VERIFICATION (Verify Step button)
VERIFICATION → AR         (Fix First — popBackStack)
VERIFICATION → COMPLETION (Proceed)
COMPLETION → HOME   (New Project)

### Key Files
app/src/main/java/com/example/copilotui/
  MainActivity.kt                    — AppNavHost, all routes
  ui/screens/ArScanState.kt          — ArScanState, DetectedPlane data classes
  ui/screens/ArCameraView.kt         — CameraX PreviewView + ARCore session loop
                                       also exposes ArFrameGeometry (VP matrices + plane meshes)
  ui/screens/EnvironmentScanScreen.kt — camera feed, Canvas plane overlay, permission request
  ui/screens/HomeDashboardScreen.kt  — 2×2 grid, Load Blueprint shows "Coming Soon" toast
  ui/screens/Common.kt               — StatusBar, NavBar (3 tabs: Projects/Scanner/Settings)

### HomeDashboardScreen Buttons
"Start New Project" → navigates to SCAN
"Continue Project"  → navigates to TIMELINE
"Load Blueprint"    → Toast "Coming Soon" (no route)
"Demo Mode"         → navigates to AR_CONSTRUCTION

### Demo Flow (Hackathon Stage)
1. Open app — splash screen, "Powered by Qualcomm AI"
2. Scan room — live camera, ARCore detects floor plane, shows dimensions
3. Tap Finish Scan — enters AR construction view
4. YOLO identifies objects (laptop=steel panel, bottle=pillar)
5. AR guides user to place objects step by step
6. User taps Verify Step — VLM confirms step complete in <2s
7. AR advances to next step
8. Structure completes — Completion screen with stats
