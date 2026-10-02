#!/usr/bin/env bash
# Compila o projeto (alvo: Java 8).
# Cenario A: javac 1.8 (faculdade) -> compila direto, JavaFX embutido.
# Cenario B: javac novo + OpenJFX -> usa jars do JavaFX no classpath e --release 8
#            (fallback: -source 8 -target 8).
set -e
cd "$(dirname "$0")"

mkdir -p build

detect_javafx_lib() {
  if [ -n "$JAVAFX_LIB" ]; then echo "$JAVAFX_LIB"; return; fi
  for d in /usr/lib/jvm/java-*-openjdk/lib /usr/lib/jvm/*/lib /usr/share/openjfx/lib /usr/lib/jvm/javafx*/lib; do
    if [ -f "$d/javafx.controls.jar" ]; then echo "$d"; return; fi
  done
  echo ""
}

if javac -version 2>&1 | grep -q '1\.8'; then
  echo "javac 1.8 detectado: compilando direto (JavaFX embutido)."
  find src -name '*.java' > sources.txt
  javac -d build @sources.txt
else
  JAVAFX_LIB="$(detect_javafx_lib)"
  if [ -z "$JAVAFX_LIB" ]; then
    echo "ERRO: JavaFX nao encontrado. Defina JAVAFX_LIB apontando para a pasta lib do OpenJFX." >&2
    exit 1
  fi
  echo "javac moderno detectado; JavaFX em: $JAVAFX_LIB"
  find src -name '*.java' > sources.txt
  CP="$JAVAFX_LIB/javafx.base.jar:$JAVAFX_LIB/javafx.graphics.jar:$JAVAFX_LIB/javafx.controls.jar"
  if ! javac --release 8 -cp "$CP" -d build @sources.txt 2>/dev/null; then
    echo "--release 8 falhou; tentando -source 8 -target 8."
    javac -source 8 -target 8 -cp "$CP" -d build @sources.txt
  fi
fi

echo "Compilacao OK."
