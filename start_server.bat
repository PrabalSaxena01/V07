@echo off
setlocal

: V07 Server Launcher
echo Starting V07 Server...

set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot
set CP=app\src\main\java

%JAVA_HOME%\bin\java.exe -cp %CP% com.v07.dashboard.V07ServerKt\$startServer -Xmx512m -XX:+UseParallelGC -Dfile.encoding=UTF-8
