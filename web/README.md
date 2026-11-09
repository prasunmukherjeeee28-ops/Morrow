# Morrow Web (React UI)

This folder is the new web-first UI experiment. Native Android remains Kotlin/Compose while this frontend is built and validated independently; it is not yet wired into the Android APK.

## Stack
- React + TypeScript + Vite
- Three.js via React Three Fiber + Drei (procedural animated 3D core)
- Motion (Framer Motion) for entrance/interaction animations
- CSS for the dark violet/glass visual system

## Run locally
```bash
cd web
npm install
npm run dev
```

Build:
```bash
npm run build
```

## Integration plan
1. Stabilize the React UI and API request/response contract.
2. Build the web bundle.
3. Package `web/dist` into Android assets and load it in a WebView.
4. Expose only narrowly-scoped native bridge methods for Android features (file picker, share, etc.).
5. Keep the Cloudflare Worker as the server-side AI boundary; never ship provider keys in web assets.

The WebView integration is deliberately a later step: it should not replace the existing Android app until API compatibility, native features, and offline/error behavior are verified.
