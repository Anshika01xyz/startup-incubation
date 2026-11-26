#!/usr/bin/env sh
set -eu
ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
BUILD="$ROOT/target/core-verification"
mkdir -p "$BUILD"
find "$ROOT/src/main/java/com/startup/platform" -name '*.java' ! -name 'Main.java' -print > "$BUILD/sources.txt"
printf '%s\n' "$ROOT/src/test/java/com/startup/platform/CoreVerification.java" >> "$BUILD/sources.txt"
javac -Xlint:all -d "$BUILD" @"$BUILD/sources.txt"
java -cp "$BUILD:$ROOT/src/main/resources" com.startup.platform.CoreVerification
