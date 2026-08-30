# -*- coding: utf-8 -*-
"""
FAZ 183c — ILERLEME PANELININ ZEMINI DUZLESTIRILIYOR.

Kullanici: "haritanin ustundeki bilgi kutusunun zemini kirli, duz olmali."

Iki ayri sey birlesip "kirli" gorunumu uretiyordu:

1. Varligin ICINDE yarim ton NOKTA DESENI var (`kb_*_panel.webp`).
2. Panel `NinePatchImage` ile 9 dilime bolunup GERILIYOR. Nokta deseni
   gerilince duzensiz bir dokuya donusuyor ve dilim sinirlarinda GORUNUR
   DIKEY/YATAY DIKISLER birakiyor (cihaz ekran goruntusunde olculdu).

Cozum: cerceve (neon kenarlik) OLDUGU GIBI kaliyor, yalnizca IC ALAN
bulaniklastirilarak nokta deseni siliniyor. Duz/yumusak bir alan gerildiginde
dikis de olusmuyor -- yani ikinci sorun da kokunden kapaniyor.

Calistir:  python tools/wordart/flatten_panel.py
"""
import os
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DRAW = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")
THEMES = ["esy", "pro", "car"]


def frame_edges(im):
    """Neon cercevenin dort kenarini PARLAKLIK TEPESINDEN bulur (tahmin degil)."""
    a = np.array(im.convert("RGBA")).astype(float)
    lum = (a[:, :, 0] * 0.299 + a[:, :, 1] * 0.587 + a[:, :, 2] * 0.114) * (a[:, :, 3] / 255.0)
    h, w = lum.shape
    row = lum[h // 2]
    col = lum[:, w // 2]
    left = int(np.argmax(row[: w // 4]))
    right = int(w - 1 - np.argmax(row[::-1][: w // 4]))
    top = int(np.argmax(col[: h // 4]))
    bottom = int(h - 1 - np.argmax(col[::-1][: h // 4]))
    return left, top, right, bottom


def flatten(path):
    """Cerceve ayni kalir; IC ALAN tek bir duz renge boyanir.

    Ilk denemede ic alan yalnizca BULANIKLASTIRILMISTI ve kullanici hakli
    olarak "hala kirli" dedi: nokta deseni cerceveye kadar geliyordu, ic
    kirpma payinin disinda kalan halka desenli kaliyordu. Ustelik 9 dilim
    GERILMESI, duz olmayan her dokuyu dilim sinirlarinda dikise ceviriyor.
    Tek renk hem desenden hem dikisten kurtariyor -- istenen de "duz".

    Ic alan maskesi DIKDORTGEN DEGIL: opak ama PARLAK OLMAYAN pikseller
    seciliyor, boylece maske yuvarlatilmis kose formunu kendiliginden takip
    ediyor ve dolgu cercevenin disina tasmiyor.
    """
    im = Image.open(path).convert("RGBA")
    a = np.array(im).astype(float)
    alpha = a[:, :, 3]
    lum = a[:, :, 0] * 0.299 + a[:, :, 1] * 0.587 + a[:, :, 2] * 0.114

    l, t, r, b = frame_edges(im)
    inner = np.zeros(lum.shape, bool)
    inner[t:b + 1, l:r + 1] = True
    frame_lum = float(np.percentile(lum[inner], 97))
    # Ic alan: opak + cerceve parlakligindan belirgin olarak sonuk.
    interior = inner & (alpha > 200) & (lum < frame_lum * 0.62)

    mask = Image.fromarray((interior * 255).astype(np.uint8))
    # Cerceveye yapismasin diye biraz iceri cekilip yumusatiliyor: aradaki
    # gecis, cercevenin ic isimasinin gorunmesini de saglar.
    for _ in range(1):
        mask = mask.filter(ImageFilter.MinFilter(3))
    mask = mask.filter(ImageFilter.GaussianBlur(2.2))

    px = a[:, :, :3][interior]
    fill = tuple(int(c) for c in np.median(px, axis=0))

    out = im.copy()
    flat = Image.new("RGBA", im.size, fill + (255,))
    out.paste(flat, (0, 0), mask)
    out.save(path, "WEBP", lossless=True, quality=100)
    return im.size, fill, int(interior.sum())


if __name__ == "__main__":
    for th in THEMES:
        p = os.path.join(DRAW, "kb_%s_panel.webp" % th)
        size, fill, n = flatten(p)
        print("kb_%s_panel %s  dolgu=#%02X%02X%02X  ic piksel=%d"
              % (th, size, fill[0], fill[1], fill[2], n))
