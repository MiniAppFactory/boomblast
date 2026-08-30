# -*- coding: utf-8 -*-
"""
FAZ 183 — SAGDAKI GOSTERGELERIN VARLIKLARI TEMIZLENIYOR.

Kullanici (cihaz fotografiyla): "sagdaki gostergeler olmamis."

OLCULEN uc ayri kusur, hepsi de VARLIK DILIMLEME hatasindan geliyor:

1. `kb_*_setbtn.webp` icinde IKI buton var. Dilimleme sirasinda komsu buton da
   kadraja girmis: Kolay'da 430px'lik bitmap'in 271..429 araligi BOS bir
   ikinci buton. Ekranda bu, disli dugmesinin sagindaki "yarim kalmis kutu"
   olarak goruluyordu -- kullanicinin gordugu sey buydu.
2. Ayni sizinti `back` ve kismen digerlerinde de var; govde bitmap icinde
   ORTALI DEGIL. `AssetImage` BITMAP'i `Ref` merkezine oturttugu icin, govde
   bitmap icinde saga/sola kaymissa dugme de kayiyor -- kupa ile disli
   arasindaki dengesiz bosluk buradan.
3. Ucunun govde/bitmap orani ve en-boy orani birbirinden farkli (0.90-1.02),
   oysa `Ref` her tema icin TEK bir oran kullaniyor. Yani en az iki temada
   olcu yanlis oluyordu.

Bu betik uc varligi da (coin / trophybtn / setbtn, uc tema) sunlari yaparak
NORMALIZE eder -- sanat yeniden cizilmez, olceklenmez, yalnizca kirpilir ve
seffaf kenar payi ile ortalanir:
  - govdeden sonraki KOMSU parca atilir,
  - govde bitmap'in TAM ORTASINA gelir,
  - govde/bitmap orani her varlikta ayni (BODY_RATIO),
  - en-boy orani varlik tipine gore ayni (TARGET_ASPECT).

Boylece `Ref.COIN / TROPHY_BTN / SET_BTN` tek bir oran cifti ile uc temada da
dogru calisir.

Calistir:  python tools/wordart/normalize_chrome.py
"""
import os
import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DRAW = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")

THEMES = ["esy", "pro", "car"]
# tip -> hedef en-boy (govde+pay). Uc temanin govde oranlarinin ortancasi.
TARGET_ASPECT = {"coin": 0.51, "trophybtn": 0.97, "setbtn": 1.00, "back": 1.00}
BODY_RATIO = 0.806          # govde genisligi / bitmap genisligi (pay %12+%12)


def body_run(alpha, thr=150, joint=6):
    """Govde sutun profilindeki EN GENIS kesintisiz parca + komsularinin siniri.

    "Ilk parca" YETMIYOR: Pro temasinin `back` varliginda butonun solunda
    14px'lik dekoratif bir benek var ve ilk parca O oluyordu -- kirpma butonu
    tamamen kacirirdi. En genis parca her varlikta gercek govdedir.
    """
    cols = (alpha > thr).sum(axis=0)
    nz = np.nonzero(cols)[0]
    runs = []
    s = p = nz[0]
    for c in nz[1:]:
        if c <= p + joint:
            p = c
        else:
            runs.append((s, p))
            s = p = c
    runs.append((s, p))
    i = max(range(len(runs)), key=lambda k: runs[k][1] - runs[k][0])
    lo = runs[i - 1][1] + 1 if i > 0 else 0
    hi = runs[i + 1][0] if i + 1 < len(runs) else None
    return int(runs[i][0]), int(runs[i][1]), int(lo), (int(hi) if hi is not None else None)


def normalize(path, aspect):
    im = Image.open(path).convert("RGBA")
    a = np.array(im)[:, :, 3]
    bx0, bx1, prev_end, next_start = body_run(a)
    rows = np.nonzero((a[:, bx0:bx1 + 1] > 150).sum(axis=1))[0]
    by0, by1 = int(rows[0]), int(rows[-1])

    bw, bh = bx1 - bx0 + 1, by1 - by0 + 1
    # Hedef tuval: govde ortada, genislikte BODY_RATIO, yukseklikte istenen en-boy.
    W = int(round(bw / BODY_RATIO))
    H = int(round(W * aspect))
    if H < bh:                       # en-boy govdeyi kirpacaksa tuvali buyut
        H = bh
        W = int(round(H / aspect))
    mx = (W - bw) // 2
    my = (H - bh) // 2

    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    # Kaynaktan, govdenin cevresindeki ISIMA payi kadar da alinir; komsu butona
    # tasmamak icin sag sinir bir sonraki parcanin basi ile kelepcelenir.
    limit = next_start if next_start is not None else im.width
    sx0 = max(bx0 - mx, prev_end)
    sx1 = min(bx1 + 1 + mx, limit)
    sy0 = max(by0 - my, 0)
    sy1 = min(by1 + 1 + my, im.height)
    piece = im.crop((sx0, sy0, sx1, sy1))
    out.alpha_composite(piece, (mx - (bx0 - sx0), my - (by0 - sy0)))
    out.save(path, "WEBP", lossless=True, quality=100)
    return im.size, out.size, (bw, bh)


def main():
    for th in THEMES:
        for kind, aspect in TARGET_ASPECT.items():
            p = os.path.join(DRAW, "kb_%s_%s.webp" % (th, kind))
            before, after, body = normalize(p, aspect)
            print("%-18s %s -> %s  body=%s  ratio=%.3f aspect=%.3f"
                  % ("kb_%s_%s" % (th, kind), before, after, body,
                     body[0] / after[0], after[1] / after[0]))


# ---------------------------------------------------------------------------
# FAZ 183b — HEDEF HAPLARI: SOLA BAKAN KUYRUK GERI GELDI
#
# Kullanici: "sag el chrome assetler kavise denk geliyor."
#
# Olculdu: `kb_*_pill_l.webp` (kuyrugu SOLA bakan, yani hapin SAGDA oldugu
# durumda kullanilan varlik) uc temada da x=0'dan basliyor -- yani KUYRUK
# TAMAMEN KIRPILMIS. Ekranda sagdaki haplar bu yuzden kuyruksuz duz bir
# dikdortgen gibi duruyor ve hangi dugume ait olduklari okunmuyordu.
#
# Yeniden cizim yok: `pill_r` (kuyrugu SAGA bakan, saglam varlik) temizlenip
# YATAY AYNALANIYOR. Iki taraf artik birebir ayni cizim, sadece yonu farkli.
PILL_ASPECT = 0.55


def normalize_pills():
    for th in THEMES:
        src = os.path.join(DRAW, "kb_%s_pill_r.webp" % th)
        before, after, body = normalize(src, PILL_ASPECT)
        im = Image.open(src).convert("RGBA")
        dst = os.path.join(DRAW, "kb_%s_pill_l.webp" % th)
        im.transpose(Image.FLIP_LEFT_RIGHT).save(dst, "WEBP", lossless=True, quality=100)
        print("%-16s %s -> %s  body=%s  ratio=%.3f  (pill_l = ayna)"
              % ("kb_%s_pill_r" % th, before, after, body, body[0] / after[0]))


if __name__ == "__main__":
    main()
    normalize_pills()
