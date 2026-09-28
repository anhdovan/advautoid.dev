@echo off
setlocal
echo Compiling AutoID C++ Sample with MSVC cl.exe...

where cl.exe >nul 2>nul
if %errorlevel% neq 0 (
    echo [NOTE] MSVC cl.exe not in PATH. Run within Visual Studio Developer Command Prompt.
    echo Alternatively, use CMake:
    echo   cmake -B build
    echo   cmake --build build --config Release
    exit /b 1
)

cl.exe /EHsc /std:c++17 /O2 /Fe:autoid_sample.exe main.cpp
if %errorlevel% equ 0 (
    echo Compilation succeeded: autoid_sample.exe
) else (
    echo Compilation failed!
    exit /b %errorlevel%
)
