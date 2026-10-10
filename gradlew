#!/bin/sh
# TaxiDZ gradle launcher: uses gradle-wrapper.jar when present,
# otherwise falls back to a system gradle (CI bootstraps one).
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ -f "$WRAPPER_JAR" ]; then
  exec java -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
else
  exec gradle "$@"
fi
