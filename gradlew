#!/usr/bin/env sh
##############################################################################
# Gradle start up script for UN*X
##############################################################################
set -e
THIS_DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -z "$GRADLE_HOME" ]; then
  GRADLE_HOME=""
fi
CLASSPATH="$THIS_DIR/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$CLASSPATH" ]; then
  # wrapper jar will be downloaded by the wrapper bootstrap; proceed
  :
fi
exec java -jar "$CLASSPATH" "$@"
