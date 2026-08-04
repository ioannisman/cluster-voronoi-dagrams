#!/usr/bin/env python3
"""Build the GitHub Pages site tree: coverage/, other/, dashboard + history."""

from __future__ import annotations

import argparse
import json
import shutil
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def _ratio(counter_type: str, root: ET.Element) -> float | None:
    for c in root.findall("counter"):
        if c.get("type") == counter_type:
            missed = int(c.get("missed", "0"))
            covered = int(c.get("covered", "0"))
            total = missed + covered
            return (covered / total) if total else 0.0
    return None


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--jacoco-xml", type=Path, required=True)
    p.add_argument("--jacoco-html-dir", type=Path, required=True)
    p.add_argument("--pages-src", type=Path, required=True, help="Repo pages/ with index.html, other/, coverage-history.json")
    p.add_argument("--out-site", type=Path, required=True)
    p.add_argument("--sha", required=True)
    p.add_argument("--date", required=True, help="ISO-8601 timestamp")
    args = p.parse_args()

    if not args.jacoco_xml.is_file():
        print(f"Missing JaCoCo XML: {args.jacoco_xml}", file=sys.stderr)
        return 1
    if not args.jacoco_html_dir.is_dir():
        print(f"Missing JaCoCo HTML dir: {args.jacoco_html_dir}", file=sys.stderr)
        return 1

    tree = ET.parse(args.jacoco_xml)
    root = tree.getroot()
    line_cov = _ratio("LINE", root)
    instr_cov = _ratio("INSTRUCTION", root)
    if line_cov is None:
        print("No LINE counter in JaCoCo report.", file=sys.stderr)
        return 1

    hist_path = args.pages_src / "coverage-history.json"
    history: list = []
    if hist_path.is_file():
        history = json.loads(hist_path.read_text(encoding="utf-8"))
    if not isinstance(history, list):
        history = []

    if not any(isinstance(e, dict) and e.get("sha") == args.sha for e in history):
        history.append(
            {
                "sha": args.sha,
                "shortSha": args.sha[:7],
                "date": args.date,
                "lineCoverage": line_cov,
                "instructionCoverage": instr_cov,
            }
        )

    hist_path.parent.mkdir(parents=True, exist_ok=True)
    hist_path.write_text(json.dumps(history, indent=2) + "\n", encoding="utf-8")

    out = args.out_site
    if out.exists():
        shutil.rmtree(out)
    out.mkdir(parents=True)

    cov_out = out / "coverage"
    cov_out.mkdir()
    shutil.copytree(args.jacoco_html_dir, cov_out, dirs_exist_ok=True)

    other_src = args.pages_src / "other"
    if other_src.is_dir():
        shutil.copytree(other_src, out / "other", dirs_exist_ok=True)

    shutil.copy2(args.pages_src / "index.html", out / "index.html")
    shutil.copy2(hist_path, out / "coverage-history.json")

    print(f"Site written to {out} ({len(history)} history point(s)).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
