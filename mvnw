#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper for Online Complaint Management System (Unix/Linux/macOS)
# ----------------------------------------------------------------------------

MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ -n "$JAVA_HOME" ]; then
    JAVA_EXE="$JAVA_HOME/bin/java"
else
    JAVA_EXE="java"
fi

exec "$JAVA_EXE" \
    -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" \
    -cp "$WRAPPER_JAR" \
    org.apache.maven.wrapper.MavenWrapperMain "$@"
