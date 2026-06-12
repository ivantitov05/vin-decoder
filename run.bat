@echo off
chcp 65001 > nul
title VIN Decoder Application
color 0A

echo ========================================
echo     VIN DECODER APPLICATION
echo ========================================
echo.

REM ========================================
REM ПЕРЕХОД В ПАПКУ ПРОЕКТА (УКАЖИТЕ ВАШ ПУТЬ!)
REM ========================================
cd /d C:\Users\Ivan\Documents\GitHub\vin-decoder

echo [INFO] Текущая папка: %CD%
echo.

REM Проверка наличия JAR файла
if not exist target\*.jar (
    echo [ОШИБКА] JAR файл не найден!
    echo.
    echo Сначала соберите проект:
    echo mvn clean package
    echo.
    pause
    exit /b 1
)

echo [INFO] Запуск VIN Decoder...
echo [INFO] Порт: 8080
echo [INFO] Swagger UI: http://localhost:8080/swagger-ui.html
echo.

REM Находим и запускаем JAR
for /f "tokens=*" %%i in ('dir /b target\*.jar 2^>nul ^| findstr /v "\.original$"') do set JAR_FILE=%%i

if defined JAR_FILE (
    echo [INFO] Запуск: %JAR_FILE%
    java -jar "target\%JAR_FILE%"
) else (
    echo [ОШИБКА] JAR файл не найден в папке target!
    dir target\
    pause
    exit /b 1
)

echo.
echo ========================================
echo     ПРИЛОЖЕНИЕ ОСТАНОВЛЕНО
echo ========================================
pause