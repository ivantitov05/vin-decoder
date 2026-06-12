@echo off
chcp 65001 > nul
title Build and Run VIN Decoder
color 0E

echo ========================================
echo     BUILD AND RUN VIN DECODER
echo ========================================
echo.

REM ========================================
REM ПУТЬ К ПРОЕКТУ (ИЗМЕНИТЕ ПРИ НЕОБХОДИМОСТИ)
REM ========================================
set PROJECT_PATH=C:\Users\Ivan\Documents\GitHub\vin-decoder

REM ========================================
REM ПУТЬ К JAVA 17 (УКАЖИТЕ ВАШ ПУТЬ)
REM ========================================
set JAVA17_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot

REM ========================================
REM ПРОВЕРКА JAVA 17
REM ========================================
echo [1/6] Проверка Java 17...
if not exist "%JAVA17_HOME%\bin\java.exe" (
    echo [ОШИБКА] Java 17 не найдена по пути: %JAVA17_HOME%
    echo.
    echo Скачайте Java 17 с https://adoptium.net/temurin/releases/?version=17
    echo Или укажите правильный путь в переменной JAVA17_HOME
    pause
    exit /b 1
)
echo [OK] Java 17 найдена

REM ========================================
REM УСТАНОВКА JAVA 17 ДЛЯ ТЕКУЩЕЙ СЕССИИ
REM ========================================
echo.
echo [2/6] Установка Java 17 для сборки...
set JAVA_HOME=%JAVA17_HOME%
set PATH=%JAVA17_HOME%\bin;%PATH%

REM Проверка версии Java
java -version 2>&1 | findstr "17" >nul
if errorlevel 1 (
    echo [ОШИБКА] Версия Java не 17
    java -version
    pause
    exit /b 1
)
echo [OK] Используется Java 17

REM ========================================
REM ПРОВЕРКА Maven
REM ========================================
echo.
echo [3/6] Проверка Maven...
where mvn >nul 2>nul
if errorlevel 1 (
    echo [ОШИБКА] Maven не найден в PATH!
    pause
    exit /b 1
)
echo [OK] Maven найден

REM ========================================
REM ПЕРЕХОД В ПАПКУ ПРОЕКТА
REM ========================================
echo.
echo [4/6] Переход в папку проекта...
cd /d "%PROJECT_PATH%"
if errorlevel 1 (
    echo [ОШИБКА] Не удалось перейти в: %PROJECT_PATH%
    pause
    exit /b 1
)
echo [OK] Текущая папка: %CD%

REM ========================================
REM ОЧИСТКА И СБОРКА
REM ========================================
echo.
echo [5/6] Сборка проекта...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ОШИБКА] Сборка не удалась!
    pause
    exit /b 1
)

REM ========================================
REM ЗАПУСК
REM ========================================
echo.
echo [6/6] Запуск приложения...
echo.
echo ========================================
echo     ПРИЛОЖЕНИЕ ЗАПУЩЕНО
echo ========================================
echo.
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo.
echo Для остановки нажмите Ctrl+C
echo ========================================
echo.

java -jar target\vin-decoder-1.0.0.jar

echo.
echo ========================================
echo     ПРИЛОЖЕНИЕ ОСТАНОВЛЕНО
echo ========================================
pause