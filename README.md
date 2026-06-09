<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/d04aefc7-3c3e-4b81-9261-8729932ff222

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Download a small on-device LLM such as Gemma 3 270M in MediaPipe task format and place it at `/data/local/tmp/llm/gemma-3-270m-it.task` on the device
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on a physical device

DiaTrack now uses the on-device MediaPipe LLM path for AI predictions and falls back to the local heuristic predictor if the model is missing.
