# Web performance

Browser timings are diagnostics, not CI pass/fail limits. Run the explorer with
`?benchmark=1` to execute two warmups and five measured frames for each supported
gallery-scene, metric, and raster-size combination. The report is written to the
console as `[cvd-benchmark-json]` and to the hidden `#cvd-benchmark-results`
element.

Record the browser, viewport, device-pixel ratio, commit, whether assets were
warm, and the median values from the generated report. Compare local and GitHub
Pages only after both builds have loaded their assets and at identical raster
sizes.

## Targets

- Interactive preview: at most 100–150 ms.
- Balanced completed frame: at most 400–500 ms.
- High-quality completed frame: at most 1 s.
- Worker response delivery: at most 20 ms.
- Canvas conversion, contours, and paint submission: at most 30 ms.
- Interaction console errors: zero.
- Warm local and Pages medians at the same raster size: within 15%.

## Results

Keep raw benchmark JSON with the review artifacts rather than treating one
machine's measurements as universal limits.

### 2026-07-31 — PR 3 decision gate

Local headless Chrome on macOS, warm Vite assets, 709×709 display, default mixed
gallery scene, diagram and skeleton enabled:

- Balanced 709×709, after two warmups and across five measured frames: median
  classify 508.6 ms, export 2.7 ms, response delivery 0.1 ms, RGBA conversion
  2.7 ms, contours 5.2 ms, paint submission 1.0 ms, total 520.6 ms.
- Interactive 384×384, after two warmups and across five measured wheel-zoom
  previews: median classify 192.5 ms and total 196.2 ms.
- A generated TeaVM full→preview→next-frame transport smoke test returned exact
  24² and 12² payloads after transferring and detaching the preceding arrays.

Transport and main-thread submission meet their targets, but classification
dominates and both preview and balanced totals miss their targets. This activates
the optional primitive classification work before considering rendering changes.
