# -*- coding: utf-8 -*-
"""
FAZ 183e — DUGUM VARLIKLARININ GOVDESI BITMAP ICINE ORTALANIYOR.

Kullanici: "kolay modun 1 rakamini ortalaman lazim yuvarlak icinde."

OLCULDU (alfa>150 govde kutusu, bitmap merkezine gore kayma):
    kb_esy_node_open  (+16.5, +14.0)   <-- gorunur olan bu
    kb_car_node_open  ( +6.5,  -0.5)
    kb_pro_node_open  ( -1.0,  -4.0)
    ...digerleri ~0

`ProgressionNode` rakami KUTUYA ortaliyor; kutu da bitmap'i ortaliyor. Bitmap
icindeki DAIRE kaymissa rakam dairenin merkezine degil bitmap'in merkezine
oturuyor -- Kolay modda 16px saga, 14px asagi kaymis bir daire, yukari-sola
kacmis bir "1" demek.

Bu betik ICERIGI kaydirarak govdeyi bitmap merkezine getirir. Bitmap OLCUSU,
govde/bitmap orani ve en-boy DEGISMEZ -- yani `Ref.NODE_*` degerlerine
dokunmak gerekmiyor ve dugumlerin ekrandaki boyu aynen kaliyor.

Calistir:  python tools/wordart/center_nodes.py
"""
import os
import sys
import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from normalize_chrome import body_run  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DRAW = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")
THEMES = ["esy", "pro", "car"]
KINDS = ["node_open", "node_lock"]


def center_body(path):
    im = Image.open(path).convert("RGBA")
    a = np.array(im)[:, :, 3]
    bx0, bx1, _, _ = body_run(a)
    rows = np.nonzero((a[:, bx0:bx1 + 1] > 150).sum(axis=1))[0]
    by0, by1 = int(rows[0]), int(rows[-1])

    dx = int(round((bx0 + bx1) / 2.0 - im.width / 2.0))
    dy = int(round((by0 + by1) / 2.0 - im.height / 2.0))
    if dx == 0 and dy == 0:
        return (0, 0)

    out = Image.new("RGBA", im.size, (0, 0, 0, 0))
    out.alpha_composite(im, dest=(max(-dx, 0), max(-dy, 0)),
                        source=(max(dx, 0), max(dy, 0)))
    out.save(path, "WEBP", lossless=True, quality=100)
    return (dx, dy)


if __name__ == "__main__":
    for th in THEMES:
        for k in KINDS:
            p = os.path.join(DRAW, "kb_%s_%s.webp" % (th, k))
            dx, dy = center_body(p)
            print("kb_%s_%-10s kaydirma=(%+d,%+d)" % (th, k, -dx, -dy))
