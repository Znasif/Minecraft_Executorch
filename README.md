# AI Construction Copilot

> **Qualcomm × Meta ExecuTorch Hackathon**
> Offline AR construction assistant — Minecraft in the real world, running entirely on-device.

Point your phone at any room. On-device AI identifies real objects, maps them to virtual building materials, and overlays a step-by-step Minecraft-style build guide — no cloud, no internet, everything on the Snapdragon 8 Elite Hexagon NPU.

---

## Demo Flow

1. **Splash** — "Powered by Qualcomm AI"
2. **Scan room** — live camera, ARCore detects floor plane and shows dimensions
3. **Finish Scan** — enter the AR construction view
4. **YOLO identifies objects** — laptop → steel panel, bottle → pillar, etc.
5. **AR overlay** guides you to place objects step by step
6. **Verify Step** — on-device VLM confirms the step is complete in < 2 s
7. **Advance** — AR moves to the next step
8. **Completion screen** — build stats, start a new project

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| AR | ARCore 1.41.0 (plane detection, spatial anchors) |
| Camera | CameraX 1.3.1 (camera-core / camera2 / lifecycle / view) |
| On-device inference | ExecuTorch + QNN backend, Snapdragon Hexagon NPU |
| Architecture | Single-module MVVM, Compose Navigation |

### AI Models (both bundled in `app/src/main/assets/`)

| Model | Format | Trigger | Purpose |
|---|---|---|---|
| YOLO mobile | `.pte` | Every frame | Detect real objects in scene |
| SmolVLM | `.pte` | Tap "Verify Step" | Reason whether construction step looks complete |

**Hard rules:**
- ARCore owns geometry (floor/plane detection, measurements, anchor positioning). AI never touches geometry.
- AI owns inference (object ID, step verification). ARCore never runs inference.
- No internet permission. No cloud calls. Ever.

---

## Prerequisites

| Requirement | Version |
|---|---|
| Android Studio | Meerkat or newer |
| Android Gradle Plugin | 9.2.1 |
| Kotlin | 2.1.0 |
| Android NDK | r26 |
| QNN SDK | 2.46.0.260424 |
| Target device | Samsung Galaxy S25 (Snapdragon 8 Elite) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

ARCore must be supported and installed on the device. The manifest declares `android.hardware.camera.ar` as **required**.

---

## Setup & Build

### 1. Clone the repo

```bash
git clone https://github.com/<org>/Minecraft_Executorch.git
cd Minecraft_Executorch
```

### 2. Place the ExecuTorch AAR

The `executorch.aar` is built separately and is not checked into the repo.

```bash
# Build it (requires QNN SDK 2.46.0.260424 + Android NDK r26)
./scripts/build_android_library.sh

# Then copy the output:
cp <executorch-build-output>/executorch.aar app/libs/executorch.aar
```

If a teammate has already built the AAR, just drop it into `app/libs/`.

### 3. Add your local SDK path

Create (or edit) `local.properties` in the project root:

```properties
sdk.dir=/path/to/your/Android/sdk
```

Android Studio usually creates this automatically. Do **not** commit `local.properties`.

### 4. Open in Android Studio

Open the project root in Android Studio. Let Gradle sync complete.

### 5. Run on device

Connect a supported device (Samsung Galaxy S25 recommended) with USB debugging enabled, then click **Run** or:

```bash
./gradlew installDebug
```

> The app requires a physical device with ARCore support. The emulator will not work.

---

## Project Structure

```
app/src/main/java/com/example/copilotui/
├── MainActivity.kt                      # AppNavHost, all routes
├── ar/
│   ├── ArCameraView.kt                  # CameraX PreviewView + ARCore session loop
│   └── ArScanState.kt                   # ArScanState, DetectedPlane data classes
├── navigation/
│   └── NavGraph.kt                      # Compose Navigation graph
├── ui/
│   ├── screens/
│   │   ├── SplashScreen.kt
│   │   ├── HomeDashboardScreen.kt
│   │   ├── EnvironmentScanScreen.kt     # Live camera + ARCore plane scan
│   │   ├── ARConstructionScreen.kt      # Main AR build view, YOLO runs here
│   │   ├── BuildTimelineScreen.kt
│   │   ├── VerificationScreen.kt        # VLM step verification
│   │   ├── CompletionScreen.kt
│   │   ├── SettingsScreen.kt
│   │   └── Common.kt                    # StatusBar, NavBar
│   ├── theme/
│   │   ├── Color.kt
│   │   └── Theme.kt
│   └── viewmodel/
│       └── ConstructionViewModel.kt
└── verification/
    └── VlmEngine.kt                     # SmolVLM inference wrapper
```

---

## Team

| Name | Email |
|---|---|
| Abby Zhang | abby.wineglass@gmail.com |
| Jacob Fobean | jacobfobean@gmail.com |
| Nasif Zaman | vznasif@gmail.com |
| Krishnakumar Vijayasankar | krishkvpersonal@gmail.com |

---

## License

```
MIT License

Copyright (c) 2026 Abby Zhang, Jacob Fobean, Nasif Zaman, Krishnakumar Vijayasankar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
