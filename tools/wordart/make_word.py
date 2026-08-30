# -*- coding: utf-8 -*-
"""
FAZ 182 — "KOLAY MOD" kelime sanati DUZ tek satira cekiliyor.

Kullanici: "kolay mod yazisi yanlis, pro ve kariyerin yazildigi gibi olmali, duz."

Eldeki `kb_esy_word.webp` IKI SATIRDI (KOLAY altin / MOD camgobegi + kup),
oysa `kb_pro_word` ve `kb_car_word` tek satir altin kelime sanati. Iki satirlik
varliktan "KOLAY"i kesip almak mumkun DEGIL: iki kelimenin mor konturu tek bir
govdede birlesmis durumda.

O yuzden kelime sanati yeniden uretiliyor -- ama tarz TAHMIN EDILMIYOR:
palet ve katman yapisi `kb_pro_word.webp`den PIKSEL ORNEKLENDI
(bkz. asagidaki sabitler), font oyunun kendi basik fontu (Baloo 2 ExtraBold,
res/font/baloo2_extrabold.ttf), parildamalar ise dogrudan PRO varligindan
KESILIP aliniyor -- yani yeni bir parildama cizilmiyor.

Cikti: app/src/main/res/drawable-nodpi/kb_esy_word.webp (430x175, RGBA)
En-boy orani 0.407 -> `Ref.WORD`un varsaydigi 0.402 ile ortusuyor; eski
varligin 0.733'luk orani yerlesimi yukari kaydiriyordu.

Calistir:  python tools/wordart/make_esy_word.py
"""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter, ImageChops

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RES = os.path.join(ROOT, "app", "src", "main", "res")
FONT = os.path.join(RES, "font", "baloo2_extrabold.ttf")
PRO = os.path.join(RES, "drawable-nodpi", "kb_pro_word.webp")
DRAW = os.path.join(RES, "drawable-nodpi")

# FAZ 184 — MOD BASLIKLARI ARTIK DILE GORE DEGISIYOR.
#
# Kullanici: "basliklar dil degisince degismiyor, turkce kaliyor." Hakli:
# `modeLabel` ve haritadaki her metin `language.pick` ile ceviriliyordu ama
# BASLIK bir RASTER varlikti (`kb_*_word.webp`) ve icinde Turkce yazi gomuluydu.
#
# Turkce varliklar OLDUGU GIBI kaliyor (kullanici onlari onayladi); bu betik
# yalnizca diger dillerin surumlerini uretir. Parildamalar her temanin KENDI
# Turkce varligindan kesiliyor, boylece dil degisince parildama dili/rengi de
# temayla tutarli kaliyor.
SPARKLE_SRC = {
    # tema -> (kaynak varlik, dort parildama kutusu, hedef kose konumlari)
    "esy": ("kb_pro_word.webp",
            [(44, 10, 84, 52), (346, 12, 386, 54), (44, 142, 82, 180), (378, 146, 414, 182)],
            [(10, 0), (382, 2), (24, 134), (370, 136)]),
    "pro": ("kb_pro_word.webp",
            [(44, 10, 84, 52), (346, 12, 386, 54), (44, 142, 82, 180), (378, 146, 414, 182)],
            [(10, 0), (382, 2), (24, 134), (370, 136)]),
    "car": ("kb_car_word.webp",
            [(330, 22, 378, 74), (0, 30, 40, 70), (135, 14, 168, 46), (0, 124, 36, 158)],
            [(376, 0), (6, 6), (150, 0), (8, 132)]),
}

# tema -> dil -> baslik metni. Turkce YOK: o varliklar korunuyor.
TITLES = {
    # Aksanli harfler Baloo 2 ExtraBold'da MEVCUT (cmap kontrol edildi:
    # A-grave, A-acute, E-grave... hepsi var), o yuzden sadelestirilmiyor.
    "esy": {"en": "EASY MODE", "it": "MODALITÀ FACILE", "fr": "MODE FACILE", "es": "MODO FÁCIL"},
    "pro": {"en": "PRO MODE", "it": "MODALITÀ PRO", "fr": "MODE PRO", "es": "MODO PRO"},
    "car": {"en": "CAREER", "it": "CARRIERA", "fr": "CARRIÈRE", "es": "CARRERA"},
}

W, H = 430, 175          # kb_car_word ile ayni oran (0.402)
S = 4                    # supersampling
CW, CH = W * S, H * S

# --- kb_pro_word'den ORNEKLENEN palet -------------------------------------
GOLD_STOPS = [           # harf govdesi, dikey gradyan
    (0.00, (255, 247, 110)),
    (0.10, (253, 222, 4)),
    (0.38, (252, 195, 2)),
    (0.68, (251, 165, 3)),
    (0.90, (249, 143, 0)),
    (1.00, (214, 92, 12)),
]
GOLD_FOOT = (165, 23, 39)      # harfin dibindeki sicak koyu serit (#A51727)
PURPLE_STOPS = [               # mor kontur, dikey gradyan
    (0.00, (91, 18, 180)),
    (0.35, (26, 1, 64)),
    (0.70, (15, 0, 51)),
    (1.00, (63, 4, 128)),
]
PURPLE_RIM = (123, 43, 232)    # konturun dis kenarindaki parlak mor
GLOW = (255, 138, 43)          # dis sicak isima

# Kalinliklar (4x uzayda)
FATTEN = 9           # Baloo 2, referans fontundan ince -- govde sismesi
EXTRUDE = 11 * S     # 3B govde: harfin asagi dogru uzatilmasi
OUT_R = 11 * S       # mor konturun kalinligi
RIM_R = 3 * S        # konturun dis kenarindaki parlak serit


def vgrad(mask, stops, box, cw=None, ch=None):
    """`mask` bolgesini, kutunun ust/alt sinirlarina gore dikey gradyanla doldur."""
    top, bot = box
    grad = Image.new("RGB", (1, max(bot - top, 1)))
    px = grad.load()
    for i in range(grad.height):
        t = i / max(grad.height - 1, 1)
        for j in range(len(stops) - 1):
            t0, c0 = stops[j]
            t1, c1 = stops[j + 1]
            if t0 <= t <= t1:
                k = (t - t0) / max(t1 - t0, 1e-6)
                px[0, i] = tuple(int(c0[n] + (c1[n] - c0[n]) * k) for n in range(3))
                break
        else:
            px[0, i] = stops[-1][1]
    cw = cw or CW
    ch = ch or CH
    layer = Image.new("RGB", (cw, ch), stops[-1][1])
    layer.paste(grad.resize((cw, grad.height), Image.NEAREST), (0, top))
    if top > 0:
        layer.paste(Image.new("RGB", (cw, top), stops[0][1]), (0, 0))
    out = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
    out.paste(layer, (0, 0), mask)
    return out


def dilate(mask, radius):
    """Yuvarlak dilatasyon: MaxFilter yigini yerine blur+esik (daire gibi yayilir)."""
    if radius <= 0:
        return mask.copy()
    b = mask.filter(ImageFilter.GaussianBlur(radius * 0.62))
    return b.point(lambda v: 255 if v > 34 else 0)


def feather(img, pad=6):
    """Kirpilan parildamanin KENARINI yumusatir.

    Parildamalar PRO varligindan kesiliyor ve etraflarindaki sicak isima
    kirpma sinirinda ANI kesiliyordu -- seffaf zeminde bu, gorunur bir KARE
    yama birakiyor. Kenar boyunca alfa sifira cekiliyor.
    """
    m = Image.new("L", img.size, 0)
    ImageDraw.Draw(m).rectangle([pad, pad, img.width - 1 - pad, img.height - 1 - pad], fill=255)
    m = m.filter(ImageFilter.GaussianBlur(pad * 0.9))
    out = img.copy()
    out.putalpha(ImageChops.multiply(img.getchannel("A"), m))
    return out


def build(text, theme, out_path):
    # 1) Metin, once GENIS bir tuvale ciziliyor; nihai yerlesim (kenar payi,
    #    dikey konum) sonra "sigdir" adiminda yapiliyor. Boylece punto aramak
    #    yerine olculen govde dogrudan hedef kutuya oturtuluyor.
    PAD = 200
    font = ImageFont.truetype(FONT, 300)
    # Kelime arasi bosluk fontun kendi bosluğundan DAR (0.26 em). Genis
    # boslukta mor kontur iki kelimenin arasinda keskin bir V CENTIGI
    # yapiyordu; referans `kb_pro_word`de kelimeler neredeyse birlesik.
    words = text.split(" ")
    gap = int(300 * 0.26)
    boxes = [font.getbbox(wd) for wd in words]
    widths = [b[2] - b[0] for b in boxes]
    total = sum(widths) + gap * (len(words) - 1)
    top = min(b[1] for b in boxes)
    bot = max(b[3] for b in boxes)
    # Calisma tuvali OLCUYE GORE aciliyor. Sabit tuvalde uzun basliklar
    # ("MODALITÀ FACILE") 300 puntoda tasip CIZIM ANINDA kirpiliyordu --
    # ekranda "IODALITÀ FACIL" olarak cikmisti. Mutlak punto onemsiz: blok
    # nasilsa sonda hedef genisligin %93'une olcekleniyor.
    BW = total + 2 * PAD
    BH = (bot - top) + 2 * PAD
    x0 = (BW - total) // 2
    y0 = (BH - (bot - top)) // 2 - top
    body = Image.new("L", (BW, BH), 0)
    d = ImageDraw.Draw(body)
    x = x0
    for wd, b, bw_ in zip(words, boxes, widths):
        d.text((x - b[0], y0), wd, font=font, fill=255)
        x += bw_ + gap
    for _ in range(FATTEN):
        body = body.filter(ImageFilter.MaxFilter(3))

    # 2) 3B govde: harfi asagi dogru sureklikle uzat.
    extrude = body.copy()
    for dy in range(1, EXTRUDE + 1):
        extrude = ImageChops.lighter(extrude, ImageChops.offset(body, 0, dy))

    # 3) Mor kontur + dis parlak serit.
    outline = dilate(extrude, OUT_R)
    rim = dilate(extrude, OUT_R + RIM_R)

    canvas = Image.new("RGBA", (BW, BH), (0, 0, 0, 0))

    # 3a) dis sicak isima
    glow = Image.new("RGBA", (BW, BH), GLOW + (0,))
    glow.putalpha(rim.filter(ImageFilter.GaussianBlur(14 * S / 4)).point(lambda v: int(v * 0.30)))
    canvas.alpha_composite(glow)

    # 3b) golge
    shadow = Image.new("RGBA", (BW, BH), (18, 0, 44, 0))
    shadow.putalpha(ImageChops.offset(rim, 0, int(4 * S)).filter(
        ImageFilter.GaussianBlur(5 * S / 2)).point(lambda v: int(v * 0.75)))
    canvas.alpha_composite(shadow)

    box = rim.getbbox()
    yspan = (box[1], box[3])

    # 3c) parlak dis serit, sonra kontur govdesi
    rim_layer = Image.new("RGBA", (BW, BH), PURPLE_RIM + (255,))
    rim_layer.putalpha(rim)
    canvas.alpha_composite(rim_layer)
    canvas.alpha_composite(vgrad(outline, PURPLE_STOPS, yspan, BW, BH))

    # 4) Altin govde + dip serit
    gbox = body.getbbox()
    canvas.alpha_composite(vgrad(body, GOLD_STOPS, (gbox[1], gbox[3]), BW, BH))
    foot = ImageChops.subtract(body, ImageChops.offset(body, 0, -int(6 * S)))
    foot_layer = Image.new("RGBA", (BW, BH), GOLD_FOOT + (255,))
    foot_layer.putalpha(foot.filter(ImageFilter.GaussianBlur(1.6 * S)).point(lambda v: int(v * 0.9)))
    canvas.alpha_composite(foot_layer)

    # 5) Ust ic parlama (cam hissi)
    top_hi = ImageChops.subtract(body, ImageChops.offset(body, 0, int(9 * S)))
    hi = Image.new("RGBA", (BW, BH), (255, 255, 235, 255))
    hi.putalpha(top_hi.filter(ImageFilter.GaussianBlur(2.0 * S)).point(lambda v: int(v * 0.45)))
    canvas.alpha_composite(hi)

    # 6) SIGDIR. `kb_pro_word`de olculdu: govde blogu tuvalin genisliginin
    #    %93'unu kapliyor ve dikey merkezi %52'de. Ayni oranlar burada da
    #    uygulaniyor -- boylece uc kelime sanati ekranda AYNI olcude duruyor.
    # FAZ 186 — OLCEK ARTIK GENISLIGE DEGIL HARF YUKSEKLIGINE GORE.
    #
    # Kullanici: "kolay mod yazisi pro ve kariyer modunun yazisina gore kucuk."
    # OLCULDU (blok yuksekligi -> ekrandaki referans birimi):
    #     kb_car_word (KARIYER, 7 harf)   98px -> 92.8
    #     kb_pro_word (PRO MOD, 6 harf)   99px -> 94.4
    #     kb_esy_word (KOLAY MOD, 8 harf) 73px -> 69.1   <-- %26 kucuk
    #
    # Kok neden: blok SABIT GENISLIGE (%93) olcekleniyordu. Kelime uzadikca
    # ayni genisligi daha cok harf paylasiyor, dolayisiyla harf boyu DUSUYOR.
    # Basliklar yan yana degil ayri ekranlarda gorulse de goz onlari
    # kiyasliyor.
    #
    # Artik hedef HARF YUKSEKLIGI (orijinal iki varligin ortalamasi, 98px).
    # Uzun bir baslik bu boyda genisligi tasiyorsa yatayda SIKISTIRILIYOR
    # (dikey boy korunur); sikistirma sinirini da asiyorsa ancak o zaman boy
    # dusuruluyor. Boylece uc mod basligi ayni agirlikta okunuyor.
    TARGET_BLOCK_H = 98.0
    MAX_BLOCK_W = W * 0.93
    MIN_SQUEEZE = 0.80

    bx = box
    block = canvas.crop(bx)
    h = TARGET_BLOCK_H
    w = block.width * (h / block.height)
    if w > MAX_BLOCK_W:
        squeeze = MAX_BLOCK_W / w
        if squeeze < MIN_SQUEEZE:
            # Sikistirma sinirinda kalan genislige gore boyu dusur.
            h = h * (MAX_BLOCK_W / (w * MIN_SQUEEZE))
        w = MAX_BLOCK_W
    block = block.resize((max(int(round(w)), 1), max(int(round(h)), 1)), Image.LANCZOS)

    small = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    small.alpha_composite(block, ((W - block.width) // 2,
                                  int(H * 0.52) - block.height // 2))

    # 7) Parildamalar: temanin KENDI Turkce varligindan KESILIYOR.
    src_name, crops, spots = SPARKLE_SRC[theme]
    src = Image.open(os.path.join(DRAW, src_name)).convert("RGBA")
    for box, pos in zip(crops, spots):
        small.alpha_composite(feather(src.crop(box)), pos)

    small.save(out_path, "WEBP", lossless=True, quality=100)
    return small.size, block.size


if __name__ == "__main__":
    for theme, langs in TITLES.items():
        for lang, text in langs.items():
            out = os.path.join(DRAW, "kb_%s_word_%s.webp" % (theme, lang))
            size, block = build(text, theme, out)
            print("kb_%s_word_%-3s %-16s %s block=%s" % (theme, lang, text, size, block))
