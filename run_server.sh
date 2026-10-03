#!/bin/bash

cd "D:\software\V07-Android"

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found in PATH"
    echo "Please install Java 17+ from Microsoft JDK"
    exit 1
fi

set JAVA_HOME="C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot"
set CP="app/src/main/java"

echo "Using Java from: $JAVA_HOME/bin/java"
echo "Classpath: $CP"
echo

echo "Starting V07 Server on port 8000..."
$JAVA_HOME/bin/java.exe -cp "$CP" com.v07.dashboard.V07ServerKt\$startServer -Xmx512m -XX:+UseParallelGC -Dfile.encoding=UTF-8

pause
