@echo off
set TOKEN=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJQb2xQb3QiLCJpYXQiOjE3ODEyNjAyNDIsImV4cCI6MTc4MTM0NjY0Mn0.vYuUQphRAAhto1HYcxGg1MAXg09-y5pTHNfpIpdTqfY

echo.
echo ========== LOGIN ==========
echo Already logged in, token: %TOKEN%

echo.
echo ========== CHECK VIN ==========
curl -X POST "http://localhost:8080/api/check?vin=WBA3B5G59ENF12345" -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json"

echo.
echo ========== HISTORY ==========
curl -X GET "http://localhost:8080/api/history" -H "Authorization: Bearer %TOKEN%"

echo.
pause