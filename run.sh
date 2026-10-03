#!/usr/bin/env bash
# Executa a aplicacao (GUI por padrao; repassa argumentos ao FloodingApp).
set -e
cd "$(dirname "$0")"

if java -version 2>&1 | grep -q '1\.8'; then
  # JDK 1.8: JavaFX embutido, sem module-path.
  java -cp build ui.FloodingApp "$@"
else
  if [ -z "$JAVAFX_LIB" ]; then
    for d in /usr/lib/jvm/java-*-openjdk/lib /usr/lib/jvm/*/lib /usr/share/openjfx/lib /usr/lib/jvm/javafx*/lib; do
      if [ -f "$d/javafx.controls.jar" ]; then JAVAFX_LIB="$d"; break; fi
    done
  fi
  java --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.graphics -cp build ui.FloodingApp "$@"
fi
