#!/bin/sh
# Gradle start-up script (simplified for CI)

DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
PROPS="$DIR/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading gradle-wrapper.jar..."
  mkdir -p "$DIR/gradle/wrapper"
  curl -sL -o "$WRAPPER_JAR" \
    "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar" \
    || curl -sL -o "$WRAPPER_JAR" \
    "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
fi

# Prefer system gradle if wrapper jar still missing
if [ ! -f "$WRAPPER_JAR" ]; then
  if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
  else
    echo "No gradle-wrapper.jar and no system gradle found"
    exit 1
  fi
fi

exec java -jar "$WRAPPER_JAR" "$@"
