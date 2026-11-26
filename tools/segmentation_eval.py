#!/usr/bin/env python3
"""Generate deterministic synthetic segmentation fixtures and evaluate the current CPU heuristic.

Standard-library only. Outputs PPM source images, PGM ground-truth/predicted masks,
and a JSON metrics report. No photographs, network access, or third-party packages.
"""
from __future__ import annotations
import argparse
import json
import math
from pathlib import Path

W = H = 128
THRESHOLD = 58.0
TRANSITION = 30.0

def ellipse_mask(cx: float, cy: float, rx: float, ry: float):
    return [[1 if ((x-cx)/rx)**2 + ((y-cy)/ry)**2 <= 1 else 0
             for x in range(W)] for y in range(H)]

def fixture(kind: str):
    truth = ellipse_mask(64, 63, 25, 43)
    pixels = []
    for y in range(H):
        row = []
        for x in range(W):
            if kind == "flat_high_contrast":
                bg = (242, 242, 242)
                fg = (35, 62, 92)
            elif kind == "similar_subject_background":
                bg = (220, 220, 220)
                fg = (195, 195, 195)
            elif kind == "varying_background":
                value = 100 + int(150 * x / (W-1))
                bg = (value, value, value)
                fg = (35, 62, 92)
            else:
                raise ValueError(kind)
            row.append(fg if truth[y][x] else bg)
        pixels.append(row)
    return pixels, truth

def median(values):
    values = sorted(values)
    return values[len(values)//2] if values else 0

def estimate(pixels):
    border = []
    for x in range(W):
        border.append(pixels[0][x])
        border.append(pixels[H-1][x])
    for y in range(1, H-1):
        border.append(pixels[y][0])
        border.append(pixels[y][W-1])
    bg = tuple(median([p[c] for p in border]) for c in range(3))
    soft = []
    binary = []
    for row in pixels:
        sr, br = [], []
        for p in row:
            distance = math.sqrt(sum((p[c]-bg[c])**2 for c in range(3)))
            value = max(0.0, min(1.0, (distance-THRESHOLD)/TRANSITION))
            sr.append(round(value*255))
            br.append(1 if value >= 0.5 else 0)
        soft.append(sr)
        binary.append(br)
    return soft, binary, bg

def write_ppm(path: Path, pixels):
    with path.open("w", encoding="ascii") as f:
        f.write(f"P3\n{W} {H}\n255\n")
        for row in pixels:
            f.write(" ".join(f"{r} {g} {b}" for r,g,b in row) + "\n")

def write_pgm(path: Path, pixels):
    with path.open("w", encoding="ascii") as f:
        f.write(f"P2\n{W} {H}\n255\n")
        for row in pixels:
            f.write(" ".join(str(v) for v in row) + "\n")

def score(truth, predicted):
    tp = sum(truth[y][x] and predicted[y][x] for y in range(H) for x in range(W))
    fp = sum(not truth[y][x] and predicted[y][x] for y in range(H) for x in range(W))
    fn = sum(truth[y][x] and not predicted[y][x] for y in range(H) for x in range(W))
    union = tp + fp + fn
    return {
        "iou": round(tp/union, 4) if union else 1.0,
        "precision": round(tp/(tp+fp), 4) if tp+fp else 0.0,
        "recall": round(tp/(tp+fn), 4) if tp+fn else 0.0,
        "true_positive": tp, "false_positive": fp, "false_negative": fn
    }

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", default="build/segmentation-eval")
    args = parser.parse_args()
    out = Path(args.output)
    out.mkdir(parents=True, exist_ok=True)
    cases = {}
    for name in ("flat_high_contrast", "similar_subject_background", "varying_background"):
        pixels, truth = fixture(name)
        soft, binary, estimated_bg = estimate(pixels)
        write_ppm(out / f"{name}.ppm", pixels)
        write_pgm(out / f"{name}-truth.pgm", [[v*255 for v in row] for row in truth])
        write_pgm(out / f"{name}-predicted.pgm", soft)
        metrics = score(truth, binary)
        cases[name] = {
            **metrics,
            "estimated_border_rgb": estimated_bg,
            "expected_behavior": "high-contrast positive control" if name == "flat_high_contrast"
                else "known limitation; must not be treated as a reliable result"
        }
    report = {
        "suite": "AKIDS-SYNTH-SEG-001",
        "dimensions": [W, H],
        "method": "CPU border-colour heuristic emulation",
        "threshold": THRESHOLD,
        "transition_width": TRANSITION,
        "cases": cases,
        "warning": "Synthetic fixtures only; this is not Android runtime evidence or real-portrait validation."
    }
    (out / "report.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))

if __name__ == "__main__":
    main()
