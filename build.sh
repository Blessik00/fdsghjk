#!/usr/bin/env bash
# Собирает pulsepvp-1.1.0.jar: старые классы из original/ + новые из src/ (компиляция по заглушкам из stubs/).
set -euo pipefail
cd "$(dirname "$0")"
GSON="${GSON:-libs/gson.jar}"
if [ ! -f "$GSON" ]; then
  mkdir -p libs
  curl -fsSL -o libs/gson.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
  GSON=libs/gson.jar
fi
rm -rf work out && mkdir -p work/base work/keep/dev/pulse/pvp out
unzip -q original/pulsepvp-1.0.0.jar -d work/base
# классы, которые остаются как были
for c in Hud Tracker DamageNumbers 'DamageNumbers$Num' Ui; do cp "work/base/dev/pulse/pvp/$c.class" work/keep/dev/pulse/pvp/; done
rm -f work/base/dev/pulse/pvp/{Config,ClickGuiScreen,PulsePvP}.class
javac --release 21 -encoding UTF-8 -proc:none -d work/compiled -cp "work/keep:$GSON" -sourcepath stubs $(find src -name '*.java')
cp -r work/compiled/dev work/base/
cp resources/* work/base/
(cd work/base && jar --create --file ../../out/pulsepvp-1.1.0.jar --manifest META-INF/MANIFEST.MF $(ls | grep -v META-INF))
echo "Готово: out/pulsepvp-1.1.0.jar"
