"""Validation/build material only. Overlay Java21 source-compiled classes; never edit originals."""
import argparse, zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('original',type=Path);p.add_argument('classes',type=Path);p.add_argument('output',type=Path);a=p.parse_args()
if a.original.resolve()==a.output.resolve():raise SystemExit('Refusing to overwrite the original')
entries={f.relative_to(a.classes).as_posix():f.read_bytes() for f in a.classes.rglob('*.class')}
if not entries:raise SystemExit('No compiled classes')
a.output.parent.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(a.original) as src,zipfile.ZipFile(a.output,'w',zipfile.ZIP_DEFLATED) as dst:
 for info in src.infolist():dst.writestr(info,entries.pop(info.filename,src.read(info.filename)))
 for name,data in entries.items():dst.writestr(name,data)
