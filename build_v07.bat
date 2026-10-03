@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;D:\Android\build-tools\34.0.0;D:\Android\platform-tools;%PATH%
.\gradlew.bat assembleDebug
