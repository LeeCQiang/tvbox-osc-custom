@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.13+11
set PATH=%JAVA_HOME%\bin;C:\Users\Administrator\AppData\Local\Android\Sdk\platform-tools;%PATH%
cd /d D:\agent\dprnix\TVBOX\tvbox-osc-dev
.\gradlew assembleNormalDebug --no-daemon
pause