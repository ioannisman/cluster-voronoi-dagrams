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

Results are appended at the PR 3 decision gate. Keep raw benchmark JSON with the
review artifacts rather than committing machine-specific data as a universal
baseline.
