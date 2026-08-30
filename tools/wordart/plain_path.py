# -*- coding: utf-8 -*-
"""
FAZ 183d — YOLUN ETRAFINDAKI "HARE" KALDIRILIYOR.

Kullanici: "baglayan cizgilerin etrafindaki hareleri kaldir, duz cizgi olsun."

Olculdu: `kb_*_path.webp` tek bir cizgi degil UC katman: ortada parlak cekirdek,
iki yaninda KOYU TEAL bir bant ve o bandin uzerinde tren rayi gibi ENINE
CENTIKLER. Ekranda "hare" olarak okunan sey bu centikli koyu bant.

Cozum: cizim yeniden cizilmiyor; koyu bant SOYULUYOR. Yontem parlaklik esigi
DEGIL (uc temanin parlaklik dagilimi birbirinden farkli, tek esik tutmuyor --
olculdu), GEOMETRIK: opak maske, alani baslangicin ~%28'ine inene kadar
asindiriliyor. Geriye tam olarak cekirdek cizgi kaliyor. Uzerine, cekirdegin
bulanik kopyasindan yumusak bir neon isima ekleniyor -- yani cizgi hala
"parliyor", ama etrafinda desen yok.

Calistir:  python tools/wordart/plain_path.py
"""
import os
import numpy as np
from PIL import Image, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DRAW = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")
THEMES = ["esy", "pro", "car"]

CORE_FRACTION = 0.46     # asindirmanin duracagi alan orani
GLOW_ALPHA = 0.40        # cekirdegin cevresine eklenen isimanin gucu


def plain(path):
    im = Image.open(path).convert("RGBA")
    a = np.array(im)
    alpha = Image.fromarray(a[:, :, 3])

    mask = alpha.point(lambda v: 255 if v > 40 else 0)
    start = np.count_nonzero(np.array(mask))
    core = mask
    steps = 0
    while np.count_nonzero(np.array(core)) > start * CORE_FRACTION and steps < 40:
        core = core.filter(ImageFilter.MinFilter(3))
        steps += 1

    # Cizgi uzerindeki dekoratif NOKTALAR asindirmadan sonra tumsek olarak
    # kaliyor ve cizgi "duz" degil yumrulu goruniyordu. Morfolojik kapama
    # (bulanikla + esikle) tumsekleri siliyor, sonra hafif bir bulanikla
    # kenar yumusatiliyor.
    core = core.filter(ImageFilter.GaussianBlur(3.0)).point(
        lambda v: 255 if v > 120 else 0)
    core_soft = core.filter(ImageFilter.GaussianBlur(1.1))

    rgb = im.convert("RGB")
    line = Image.new("RGBA", im.size, (0, 0, 0, 0))
    line.paste(rgb, (0, 0), core_soft)

    # Isima: cekirdegin bulanik kopyasi, cizginin KENDI renginde.
    glow_a = core_soft.filter(ImageFilter.GaussianBlur(9)).point(
        lambda v: int(v * GLOW_ALPHA))
    px = np.array(rgb).reshape(-1, 3)[np.array(core).reshape(-1) > 0]
    tint = tuple(int(c) for c in px.mean(axis=0)) if len(px) else (255, 255, 255)
    glow = Image.new("RGBA", im.size, tint + (0,))
    glow.putalpha(glow_a)

    out = Image.alpha_composite(glow, line)
    out.save(path, "WEBP", lossless=True, quality=100)
    return steps, start, np.count_nonzero(np.array(core)), tint


if __name__ == "__main__":
    for th in THEMES:
        p = os.path.join(DRAW, "kb_%s_path.webp" % th)
        steps, before, after, tint = plain(p)
        print("kb_%s_path asindirma=%dpx  alan %d -> %d (%.0f%%)  ton=#%02X%02X%02X"
              % (th, steps, before, after, 100.0 * after / before, *tint))
