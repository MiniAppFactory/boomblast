# -*- coding: utf-8 -*-
"""
generate_ui_assets.py — Kaboom Blocks UI gorsel varlik ureticisi.

Calistir:  py tools/generate_ui_assets.py
Gereksinim: Python 3.12 + Pillow (bu makinede kurulu, ek kurulum gerekmez)

NE URETIR
---------
1) Ayarlar satir glifleri (kb_ic_*)  — GameIconTile'in gradyan dolgulu
   yuvarlatilmis kare kutucugunun ICINE giren, seffaf zeminli semboller.
   Isletim sistemi emojilerinin (SPEAKER/EYE/VIBRATE/MOON/SUN) yerine gecer.
2) Arka plan bloklari (kb_block_*) — mockup'larin imzasi olan parlak 3B
   kupler; tekil ve 2-3 kuplik gruplar, seffaf zeminli.

URETMEDIGI SEYLER (bilerek)
---------------------------
- Mod madalyon sembolleri: icon_endless / icon_career / icon_pro /
  icon_comfort olarak ZATEN var ve ModeSelectScreen'de kullaniliyor.
  Yeniden uretmek calisan bir tasarimi bozardi.
- icon_coin, ic_launcher*: dokunulmuyor.

TASARIM KARARLARI
-----------------
- Cikti klasoru res/drawable-nodpi/ (TEK boy). Bu varliklar ekran
  yogunluguna gore degil, Compose'da verilen dp boyutuna gore olceklenir;
  5 yogunluk klasorune cogaltmak APK'yi bosuna sisirirdi. Kaynak boyutlar
  en buyuk kullanim x xxxhdpi (4x) uzerinden secildi.
- Gliflerin ic detaylari (goz bebegi, telefon ekrani) SEFFAF DELIK olarak
  aciliyor; boylece altindaki kutucugun gradyan rengi icinden gorunuyor —
  hedef mockup'taki gorunum tam olarak bu.
- Blok renkleri ui/theme/Color.kt'den birebir alindi. Uydurma renk yok.
"""

from PIL import Image, ImageDraw, ImageFilter
from pathlib import Path
import math

# --------------------------------------------------------------------------
# Yol / sabitler
# --------------------------------------------------------------------------

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
OUT.mkdir(parents=True, exist_ok=True)

SS = 6                 # supersampling: her sey 6x cizilip LANCZOS ile kuculur
GLYPH = 160            # glif final kenar boyu (40dp tile x 4 = 160px, xxxhdpi)

# ui/theme/Color.kt — Faz 157 blok paleti
BLOCKS = {
    "orange": (0xFF, 0x7A, 0x1E),   # BlockOrange
    "blue":   (0x3D, 0x8B, 0xFF),   # BlockBlue
    "green":  (0x2B, 0xD9, 0x68),   # BlockGreen
    "yellow": (0xFF, 0xC0, 0x1E),   # BlockYellow
    "purple": (0xB4, 0x5C, 0xFF),   # BlockPurple
}

WHITE_TOP = (255, 255, 255)
WHITE_BOT = (202, 218, 247)         # hafif soguk gri-mavi: hacim hissi
GOLD_TOP = (255, 228, 128)
GOLD_BOT = (255, 162, 26)

report = []


# --------------------------------------------------------------------------
# Yardimcilar
# --------------------------------------------------------------------------

def lerp(c, t, k):
    return tuple(round(a + (b - a) * k) for a, b in zip(c, t))


def vgrad(w, h, c_top, c_bot):
    """Dikey gradyan RGB goruntu. 1px sutun uretip resize ediyoruz (hizli)."""
    col = Image.new("RGB", (1, max(2, h)))
    px = col.load()
    for y in range(col.height):
        k = y / (col.height - 1)
        px[0, y] = lerp(c_top, c_bot, k)
    return col.resize((w, h), Image.BILINEAR)


def shade(mask, c_top, c_bot):
    """L modundaki maskeyi dikey gradyanla boyayip RGBA dondurur."""
    img = vgrad(mask.width, mask.height, c_top, c_bot).convert("RGBA")
    img.putalpha(mask)
    return img


def rpoly(d, pts, fill, r):
    """Yuvarlatilmis kose poligon. Pillow'da hazir yok: poligonu cizip
    konturunu 'curve' birlesimli kalin cizgiyle sismek en temiz yol."""
    d.polygon(pts, fill=fill)
    if r > 0:
        # DIKKAT: joint="curve" baslangic/bitis noktasini birlestirmez.
        # pts[1] tekrar eklenmezse ilk kosede centik kalir.
        d.line(list(pts) + [pts[0], pts[1]], fill=fill,
               width=int(r * 2), joint="curve")


def rrect(d, box, fill, r):
    d.rounded_rectangle(box, radius=r, fill=fill)


def arc_ring(d, cx, cy, radius, a0, a1, width, fill):
    d.arc([cx - radius, cy - radius, cx + radius, cy + radius],
          a0, a1, fill=fill, width=int(width))


def sparkle(d, cx, cy, s, fill):
    """4 uclu isilti (ay glifinde kullaniliyor)."""
    k = s * 0.30
    d.polygon([(cx, cy - s), (cx + k, cy - k), (cx + s, cy),
               (cx + k, cy + k), (cx, cy + s), (cx - k, cy + k),
               (cx - s, cy), (cx - k, cy - k)], fill=fill)


def punch(img, shape_fn):
    """Alfa kanalinda seffaf delik acar (kutucugun rengi icinden gorunsun)."""
    a = img.getchannel("A")
    d = ImageDraw.Draw(a)
    shape_fn(d)
    img.putalpha(a)
    return img


def drop_shadow(img, blur, offset, alpha):
    """Glifin altina yumusak koyu golge — renkli kutucuk uzerinde ayrisir."""
    sh = Image.new("RGBA", img.size, (0, 0, 0, 0))
    sh.putalpha(img.getchannel("A").point(lambda v: v * alpha // 255))
    sh = sh.filter(ImageFilter.GaussianBlur(blur))
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    out.alpha_composite(sh, (0, offset))
    out.alpha_composite(img)
    return out


def finish(img, target, name, lossless, note):
    """Alfa kirp -> hedef boya olcekle -> WebP yaz -> raporla."""
    box = img.getbbox()
    if box:
        img = img.crop(box)
    if img.width > target:
        h = max(1, round(img.height * target / img.width))
        img = img.resize((target, h), Image.LANCZOS)
    p = OUT / f"{name}.webp"
    if lossless:
        img.save(p, "WEBP", lossless=True, method=6)
    else:
        img.save(p, "WEBP", quality=88, method=6, exact=False)
    report.append((name, img.width, img.height, p.stat().st_size, note))
    return p


# --------------------------------------------------------------------------
# 1) AYARLAR SATIR GLIFLERI
# --------------------------------------------------------------------------

W = GLYPH * SS          # 960 calisma tuvali


def blank():
    m = Image.new("L", (W, W), 0)
    return m, ImageDraw.Draw(m)


def px(v):
    return v * W


def glyph_speaker():
    """Hoparlor + 3 ses dalgasi (Ses Efektleri satiri)."""
    m, d = blank()
    rrect(d, [px(.14), px(.395), px(.36), px(.605)], 255, px(.035))
    rpoly(d, [(px(.30), px(.415)), (px(.545), px(.175)),
              (px(.545), px(.825)), (px(.30), px(.585))], 255, px(.030))
    for rad in (.155, .235, .315):
        arc_ring(d, px(.545), px(.50), px(rad), -52, 52, px(.048), 255)
    return shade(m, WHITE_TOP, WHITE_BOT)


def glyph_eye():
    """Badem goz; iris SEFFAF delik, uzerinde beyaz parilti (Gorunum)."""
    m, d = blank()
    a, s = .335, .205                      # yari genislik / sagitta
    R = (a * a + s * s) / (2 * s)
    pts = []
    for sgn in (1, -1):                    # ust ve alt yay
        cy = .5 + sgn * (R - s)
        rng = range(0, 61) if sgn == 1 else range(60, -1, -1)
        for i in rng:
            t = -a + (2 * a) * i / 60
            y = cy - sgn * math.sqrt(max(0.0, R * R - t * t))
            pts.append((px(.5 + t), px(y)))
    d.polygon(pts, fill=255)
    img = shade(m, WHITE_TOP, WHITE_BOT)
    img = punch(img, lambda dd: dd.ellipse(
        [px(.5 - .125), px(.5 - .125), px(.5 + .125), px(.5 + .125)], fill=0))
    hi, hd = blank()
    hd.ellipse([px(.545), px(.415), px(.615), px(.485)], fill=255)
    img.alpha_composite(shade(hi, WHITE_TOP, WHITE_TOP))
    return img


def glyph_vibrate():
    """Titresen telefon: govde beyaz, ekran seffaf, iki yanda dalga."""
    m, d = blank()
    rrect(d, [px(.345), px(.135), px(.655), px(.865)], 255, px(.072))
    for cx, a0, a1 in ((.345, 132, 228), (.655, -48, 48)):
        for rad in (.175, .255):
            arc_ring(d, px(cx), px(.50), px(rad), a0, a1, px(.050), 255)
    img = shade(m, WHITE_TOP, WHITE_BOT)
    img = punch(img, lambda dd: dd.rounded_rectangle(
        [px(.393), px(.205), px(.607), px(.795)], radius=px(.040), fill=0))
    return img


def glyph_moon():
    """Hilal + isilti (Koyu Mod). Altin: mockup'ta pilin uzerinde duruyor."""
    m, d = blank()
    d.ellipse([px(.13), px(.17), px(.79), px(.83)], fill=255)
    d.ellipse([px(.36), px(.06), px(.98), px(.68)], fill=0)
    sparkle(d, px(.80), px(.755), px(.105), 255)
    sparkle(d, px(.855), px(.365), px(.058), 255)
    return shade(m, GOLD_TOP, GOLD_BOT)


def glyph_sun():
    """Gunes diski + 8 isin (Acik Mod)."""
    m, d = blank()
    d.ellipse([px(.30), px(.30), px(.70), px(.70)], fill=255)
    for i in range(8):
        a = math.radians(i * 45)
        x0, y0 = .5 + .275 * math.cos(a), .5 + .275 * math.sin(a)
        x1, y1 = .5 + .425 * math.cos(a), .5 + .425 * math.sin(a)
        d.line([px(x0), px(y0), px(x1), px(y1)], fill=255,
               width=int(px(.062)))
        d.ellipse([px(x1) - px(.031), px(y1) - px(.031),
                   px(x1) + px(.031), px(y1) + px(.031)], fill=255)
    return shade(m, GOLD_TOP, GOLD_BOT)


GLYPHS = [
    ("kb_ic_speaker", glyph_speaker, "Ses Efektleri satiri"),
    ("kb_ic_eye",     glyph_eye,     "Gorunum satiri"),
    ("kb_ic_vibrate", glyph_vibrate, "Titresim satiri"),
    ("kb_ic_moon",    glyph_moon,    "Koyu Mod / tema satiri"),
    ("kb_ic_sun",     glyph_sun,     "Acik Mod"),
]


# --------------------------------------------------------------------------
# 2) ARKA PLAN BLOKLARI
# --------------------------------------------------------------------------

ISO = 0.52          # izometrik dikey sikistirma
SIDE = 0.56         # kup yan yuz yuksekligi / genislik (izometrik kup orani)


def cube_layer(canvas, ox, oy, w, base):
    """Tek kupu (ox,oy) merkezli olarak canvas'a cizer.
    Ust yuz en acik, sol yuz orta, sag yuz koyu; ustte parlaklik,
    altta golge — brief'teki '3B parlak blok' tarifi."""
    hw = w / 2.0
    hh = hw * ISO
    sh = w * SIDE
    r = w * 0.070

    top = [(ox, oy - hh), (ox + hw, oy), (ox, oy + hh), (ox - hw, oy)]
    left = [(ox - hw, oy), (ox, oy + hh), (ox, oy + hh + sh), (ox - hw, oy + sh)]
    right = [(ox, oy + hh), (ox + hw, oy), (ox + hw, oy + sh), (ox, oy + hh + sh)]

    for pts, c_top, c_bot in (
        (left,  lerp(base, (255, 255, 255), .04), lerp(base, (0, 0, 0), .30)),
        (right, lerp(base, (0, 0, 0), .18),       lerp(base, (0, 0, 0), .52)),
        (top,   lerp(base, (255, 255, 255), .46), lerp(base, (255, 255, 255), .10)),
    ):
        m = Image.new("L", canvas.size, 0)
        rpoly(ImageDraw.Draw(m), pts, 255, r)
        canvas.alpha_composite(shade(m, c_top, c_bot))

    # Ust yuz uzerinde yumusak parlaklik lekesi ("seker" gorunumu)
    g = Image.new("L", canvas.size, 0)
    ImageDraw.Draw(g).ellipse(
        [ox - hw * .46, oy - hh * .62, ox + hw * .06, oy + hh * .02], fill=255)
    g = g.filter(ImageFilter.GaussianBlur(w * 0.05))
    gm = Image.new("L", canvas.size, 0)
    rpoly(ImageDraw.Draw(gm), top, 255, r)
    from PIL import ImageChops
    g = ImageChops.multiply(g, gm).point(lambda v: v * 105 // 255)
    canvas.alpha_composite(shade(g, (255, 255, 255), (255, 255, 255)))

    # Sadece ustteki iki kenarda ince acik kontur — isik yukaridan geliyor.
    # Tum diamond'i cercevelemek "resim cercevesi" gibi duruyordu.
    e = Image.new("L", canvas.size, 0)
    ImageDraw.Draw(e).line([top[3], top[0], top[1]], fill=150,
                           width=int(w * 0.026), joint="curve")
    canvas.alpha_composite(shade(e, lerp(base, (255, 255, 255), .70),
                                 lerp(base, (255, 255, 255), .55)))


def build_cluster(cells, w, tilt):
    """cells: [(gx, gy, gz, renk_adi)] izometrik izgara koordinatlari."""
    hw, hh, sh = w / 2.0, w / 2.0 * ISO, w * SIDE
    pos = []
    for gx, gy, gz, name in cells:
        x = (gx - gy) * hw
        y = (gx + gy) * hh - gz * sh
        pos.append((x, y, gx + gy + gz, name))
    xs = [p[0] for p in pos]
    ys = [p[1] for p in pos]
    pad = w * 0.35
    cw = int(max(xs) - min(xs) + w + pad * 2)
    ch = int(max(ys) - min(ys) + w * ISO + sh + pad * 2)
    canvas = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
    for x, y, depth, name in sorted(pos, key=lambda p: p[2]):
        cube_layer(canvas, x - min(xs) + pad + hw, y - min(ys) + pad + hh,
                   w, BLOCKS[name])
    if tilt:
        canvas = canvas.rotate(tilt, resample=Image.BICUBIC, expand=True)
    return canvas


SINGLES = [("orange", -11), ("blue", 8), ("green", -17),
           ("yellow", 14), ("purple", 5)]

CLUSTERS = [
    ("kb_block_pair_a",  [(0, 0, 0, "orange"), (1, 0, 0, "blue")], -9,
     "2'li yatay — ekran ust kosesi"),
    ("kb_block_pair_b",  [(0, 0, 0, "purple"), (0, 0, 1, "yellow")], 12,
     "2'li dikey istif — kenar"),
    ("kb_block_trio_a",  [(0, 0, 0, "green"), (0, 1, 0, "yellow"),
                          (0, 0, 1, "purple")], -6,
     "3'lu L — alt kose"),
    ("kb_block_trio_b",  [(0, 0, 0, "blue"), (1, 0, 0, "green"),
                          (1, 0, 1, "orange")], 10,
     "3'lu basamak — ust kose"),
]


# --------------------------------------------------------------------------
# Calistir
# --------------------------------------------------------------------------

def main():
    print("== Ayarlar glifleri ==")
    for name, fn, note in GLYPHS:
        img = fn()
        img = drop_shadow(img, blur=W * 0.016, offset=int(W * 0.018), alpha=110)
        finish(img, GLYPH, name, lossless=True, note=note)

    print("== Arka plan bloklari ==")
    CW = 240 * 3          # calisma cozunurlugu (kup genisligi)
    for color, tilt in SINGLES:
        img = build_cluster([(0, 0, 0, color)], CW, tilt)
        finish(img, 236, f"kb_block_{color}", lossless=False,
               note=f"tekil kup ({color})")
    for name, cells, tilt, note in CLUSTERS:
        img = build_cluster(cells, CW, tilt)
        finish(img, 400, name, lossless=False, note=note)

    total = sum(r[3] for r in report)
    print()
    print(f"{'dosya':26s} {'boyut':>11s} {'KB':>7s}  aciklama")
    print("-" * 78)
    for name, w, h, size, note in report:
        print(f"{name+'.webp':26s} {w:4d}x{h:<6d} {size/1024:6.1f}  {note}")
    print("-" * 78)
    print(f"TOPLAM {len(report)} dosya  {total/1024:.1f} KB  "
          f"({total/1024/1024:.2f} MB / 1.50 MB butce)")


if __name__ == "__main__":
    main()
