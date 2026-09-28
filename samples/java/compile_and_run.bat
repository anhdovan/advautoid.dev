@echo off
setlocal
echo ==============================================================
echo  Compiling and Running AutoID Java 21 Sample (FFM API)
echo ==============================================================

where javac >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] JDK 21+ is not found in PATH.
    echo Please install Java 21 or set JAVA_HOME.
    exit /b 1
)

if not exist bin mkdir bin

echo Compiling Java sources...
javac --enable-preview --release 21 -d bin com/beetech/adv/smartsdk/*.java ReaderApiSample.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b %errorlevel%
)

echo Running ReaderApiSample...
java --enable-preview --enable-native-access=ALL-UNNAMED -cp bin ReaderApiSample
