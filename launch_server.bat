# V07 Server Launcher Script
# This script starts the V07 HTTP server for the Android app

# Set the Java path
JAVA_HOME="C:\\Program Files\\Microsoft\\jdk-17.0.19.10-hotspot"

# Check if java.exe exists in JAVA_HOME/bin
if [ ! -f "$JAVA_HOME/bin/java.exe" ]; then
    echo "Java not found at $JAVA_HOME/bin/java.exe"
    echo "Please check the JDK installation."
    exit 1
fi

# Build the classpath
cd "D:\software\V07-Android"
set CP="app\src\main\java"

# Start the server
echo "Starting V07 Server..."
echo "Java: $JAVA_HOME/bin/java.exe"
echo "Classpath: $CP"
echo "Command: $JAVA_HOME/bin/java.exe -cp $CP com.v07.dashboard.V07ServerKt\$startServer -Xmx512m -XX:+UseParallelGC -Dfile.encoding=UTF-8"

echo
$JAVA_HOME/bin/java.exe -cp "$CP" com.v07.dashboard.V07ServerKt\$startServer -Xmx512m -XX:+UseParallelGC -Dfile.encoding=UTF-8