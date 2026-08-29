# -*- coding: utf-8 -*-
"""
ChatGPT ile uretilen UI varlik paketini projeye alir.

Kaynak : docs/ui_mockups/assetpack/*.png   (paketten cikan orijinaller)
Hedef  : app/src/main/res/drawable-nodpi/kb_*.webp

Neden yeniden boyutlandiriyoruz: orijinaller 4.5 MB PNG. Her varlik ekranda
belli bir dp boyutunda kullaniliyor; kaynak cozunurluk "en buyuk kullanim x
xxxhdpi (4x)" olarak secildi. Daha buyugu APK'yi sisirir, kucugu bulaniklasir.

Neden nodpi: bunlar dp ile boyutlanan UI varliklari. Bes yogunluk klasorune
cogaltmak APK'yi 5x sisirir, kazanc yok.
"""
import io, os
from PIL import Image

SRC = "../docs/ui_mockups/assetpack"
DST = "app/src/main/res/drawable-nodpi"

# (kaynak, hedef ad, hedef genislik px, kalite)
# hedef genislik = ekrandaki en buyuk dp kullanimi x 4 (xxxhdpi)
# NOT (2026-08-29 kullanici karari):
#  - app_icon ALINMAZ: yeni kaboom_blocks_logo.png patlayan "B" blogunu ZATEN
#    iceriyor, yani ikon + wordmark tek varlik. Ayri ikon ikisinin toplamindan
#    daha kotu duruyordu.
#  - particle_* ALINMAZ: ucusan parcalar icin bizim uretttigimiz kb_block_*
#    varliklari kullanilacak (zaten GameScreenBackground'a bagli).
PLAN = [
    ("kaboom_blocks_logo.png", "kb_logo",            720, 90),   # ~180dp genis wordmark
    ("mode_endless.png",       "kb_mode_endless",    360, 90),   # ~90dp madalyon
    ("mode_career.png",        "kb_mode_career",     360, 90),
    ("mode_pro.png",           "kb_mode_pro",        360, 90),
    ("mode_easy.png",          "kb_mode_easy",       360, 90),
    ("coin.png",               "kb_coin",            128, 92),   # ~32dp jeton
    ("trophy.png",             "kb_trophy",          176, 92),   # ~44dp buton ikonu
    ("settings.png",           "kb_settings",        176, 92),
]


# Madalyon normalizasyonu
# ------------------------
# Kaynak dort gorselin tuval yuksekligi (350/350/315/315) ve disk konumu
# farkliydi: disk merkezi Y'de 35.5 px, cap 13 px saciyordu. Ekranda haleler
# ayni hizaya oturmuyordu.
#
# Ilk denemede diski ortak tuvale ORTALADIM ama kaynagin halesi kendi tuval
# kenarinda ZATEN kesikti; o kesik KARE kenar yeni tuvalin icine dusunce
# madalyonun yuvarlakligini bozan keskin bir cizgi olarak goruldu (cihazda
# dogrulandi). Radyal yumusatma kare bir kenari temizleyemez.
#
# Cozum: kesik haleyi hic tasima — diski, cevresinde SABIT oranli kucuk bir
# pay birakarak KIRP. Geriye yalnizca diskin kendi net kenari kalir, ki o
# zaten olmasi gereken sey. Yan fayda: piksellerin neredeyse tamami diske
# ayrildigi icin ayni ekran boyutunda daha keskin gorunur.
# Madalyon normalizasyonu
# ------------------------
# Uc denemede ogrenilenler (hepsi cihazda goruldu):
#  1) Kaynak dort gorselin tuval yuksekligi ve disk konumu farkliydi -> haleler
#     ayni hizaya oturmuyordu.
#  2) Diski ortak tuvale ortalayinca, kaynagin KARE kesik hale kenari yeni
#     tuvalin icine dustu -> madalyonun yuvarlakligini bozan keskin cizgi.
#  3) Oranli kirpma da yetmedi: bazi gorsellerde dis hale alfa>200 esiginin
#     USTUNDE, yani "disk" sanilan kutu haleyi de kapsiyor ve kirpma sinirini
#     tam kare kenarin uzerine getiriyordu (KARIYER ve KOLAY MOD'da gorunur
#     kare kutu olarak cikti).
#
# Kesin cozum: DAIRESEL MASKE. Disk yaricapinin biraz disinda her sey tamamen
# saydam yapilir, gecis birkac pikselde yumusatilir. Kare bir kenar artik
# YAPISAL OLARAK imkansiz — cikti dairenin disinda hicbir piksel tasimaz.
# Bedeli: kaynagin dis halesi kayboluyor. Kabul edilebilir, cunku diskin kendi
# kenar parlamasi zaten iceride ve kart kendi parlamasini sagliyor.
MEDALLION_CANVAS = 348   # dordunun de ortak kare tuvali (px)
DISC_FILL        = 0.86  # disk capi / tuval — cevrede yumusama payi birakir

def normalize_medallion(im):
    import math
    a = im.split()[3].point(lambda v: 255 if v > 200 else 0)
    bb = a.getbbox()
    cx, cy = (bb[0] + bb[2]) / 2.0, (bb[1] + bb[3]) / 2.0
    src_r = max(bb[2] - bb[0], bb[3] - bb[1]) / 2.0

    target_r = MEDALLION_CANVAS * DISC_FILL / 2.0
    scale = target_r / src_r
    im = im.resize((max(1, round(im.width * scale)), max(1, round(im.height * scale))),
                   Image.LANCZOS)
    cx *= scale
    cy *= scale

    canvas = Image.new("RGBA", (MEDALLION_CANVAS, MEDALLION_CANVAS), (0, 0, 0, 0))
    canvas.alpha_composite(im, (round(MEDALLION_CANVAS / 2 - cx),
                                round(MEDALLION_CANVAS / 2 - cy)))

    # Dairesel maske: target_r'ye kadar dokunma, oradan 6 px'te sifira in.
    R = MEDALLION_CANVAS / 2.0
    inner, outer = target_r, target_r + 6.0
    px = canvas.load()
    for y in range(MEDALLION_CANVAS):
        dy = y - R + 0.5
        for x in range(MEDALLION_CANVAS):
            dx = x - R + 0.5
            d = math.hypot(dx, dy)
            if d <= inner:
                continue
            r, g, b, al = px[x, y]
            if d >= outer:
                px[x, y] = (r, g, b, 0)
            else:
                t = (d - inner) / (outer - inner)
                px[x, y] = (r, g, b, int(al * (1.0 - t)))
    return canvas

os.makedirs(DST, exist_ok=True)
total = 0
print("  %-22s %-12s %10s" % ("hedef", "boyut", "bayt"))
for src, name, w, q in PLAN:
    p = os.path.join(SRC, src)
    im = Image.open(p).convert("RGBA")
    if name.startswith("kb_mode_"):
        im = normalize_medallion(im)
    # ASLA BUYUTME: kaynaktan buyuk bir hedef istendiginde yeniden orneklemek
    # detay EKLEMEZ — yalnizca dosyayi sisirir ve kenarlari yumusatir. Kaynak
    # zaten hedeften kucukse oldugu gibi birakilir ve uyari basilir.
    if im.width > w:
        h = max(1, round(im.height * w / im.width))
        im = im.resize((w, h), Image.LANCZOS)
    elif im.width < w:
        print("    ! %s kaynagi hedeften kucuk (%d < %d) — oldugu gibi birakildi"
              % (src, im.width, w))
    out = os.path.join(DST, name + ".webp")
    im.save(out, "WEBP", quality=q, method=6)
    sz = os.path.getsize(out); total += sz
    print("  %-22s %-12s %10d" % (name + ".webp", "%dx%d" % im.size, sz))
print("  %-22s %-12s %10d  (%.2f MB)" % ("TOPLAM", "", total, total/1048576))
