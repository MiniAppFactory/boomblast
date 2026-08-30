# -*- coding: utf-8 -*-
"""
FAZ 186b — ISLENEN VARLIKLAR TEKRAR KAYIPLI WEBP'E CEVRILIYOR.

Faz 183-186'daki uretici/duzeltici betikler ciktilarini `lossless=True` ile
yazdi. Orijinal varliklar KAYIPLI webp'ti; sonuc olarak paket sismisti
(olculdu: kb_esy_coin 27.0 KB -> 65.5 KB, kb_esy_panel 27.9 -> 45.2,
kb_esy_path 61.0 -> 72.7). Bu bir kazanim degil, benim ekledigim regresyon.

Bu betik SADECE bu turda degisen/eklenen varliklari yuksek kaliteli kayipli
webp'e (q=92, method=6) cevirir. Alfa korunur. Tek nesil kayip; kaynak zaten
elde uretildigi icin gerekirse betikler yeniden calistirilabilir.

Calistir:  python tools/wordart/reencode_webp.py
"""
import os
import subprocess
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DRAW = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")

QUALITY = 92


# Faz 183-186 varlik isinin BASLADIGI commit. Degisen dosyalar buna gore
# bulunuyor; `git status` yetmez cunku is zaten commitlenmis olabiliyor.
BASE_REF = "5024913"


def changed_files(base=BASE_REF):
    """`base` ile calisma agaci arasinda degisen/eklenen webp'ler."""
    out = subprocess.run(
        ["git", "diff", "--name-only", base, "--",
         "app/src/main/res/drawable-nodpi"],
        cwd=ROOT, capture_output=True, text=True).stdout
    names = [os.path.basename(l.strip()) for l in out.splitlines()
             if l.strip().endswith(".webp")]
    return sorted(set(names))


if __name__ == "__main__":
    total_before = total_after = 0
    for name in changed_files():
        p = os.path.join(DRAW, name)
        if not os.path.exists(p):
            continue
        before = os.path.getsize(p)
        im = Image.open(p).convert("RGBA")
        im.save(p, "WEBP", quality=QUALITY, method=6)
        after = os.path.getsize(p)
        total_before += before
        total_after += after
        print("%-22s %7d -> %7d  (%+.0f%%)" % (name, before, after,
                                               100.0 * (after - before) / before))
    if total_before:
        print("TOPLAM %d -> %d bayt (%+.1f KB)"
              % (total_before, total_after, (total_after - total_before) / 1024.0))
