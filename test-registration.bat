@echo off
REM Registration API Test Script
REM Tests POST /api/v1/auth/register and POST /api/v1/auth/register/organization

setlocal enabledelayedexpansion

set API_URL=http://localhost:5000/api/v1/auth
set TIMESTAMP=%DATE:~10,4%%DATE:~4,2%%DATE:~7,2%_%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%

echo ============================================================
echo.  Registration API Test Suite
echo ============================================================
echo.
echo API_URL: %API_URL%
echo.

REM Test 1: User Registration
echo [1/3] Testing User Registration...
echo.

curl -X POST %API_URL%/register ^
  -H "Content-Type: application/json" ^
  -d "{ ^
    \"username\": \"testuser!TIMESTAMP!\", ^
    \"email\": \"testuser!TIMESTAMP!@example.com\", ^
    \"password\": \"TestPassword123\", ^
    \"fullName\": \"Test User\", ^
    \"birthDate\": \"2000-01-15\", ^
    \"phone\": \"+994512345678\", ^
    \"university\": \"ADA University\", ^
    \"major\": \"Computer Science\", ^
    \"course\": \"3\", ^
    \"interests\": \"web development,blockchain,AI\", ^
    \"skills\": \"TypeScript,React,Node.js\" ^
  }"

echo.
echo.
echo ============================================================
echo.

REM Test 2: Organization Registration
echo [2/3] Testing Organization Registration...
echo.

curl -X POST %API_URL%/register/organization ^
  -H "Content-Type: application/json" ^
  -d "{ ^
    \"organizationName\": \"Tech Innovation Hub %TIMESTAMP%\", ^
    \"category\": \"technology\", ^
    \"description\": \"A community for tech enthusiasts and innovators to collaborate on meaningful projects.\", ^
    \"password\": \"OrgPassword123\" ^
  }"

echo.
echo.
echo ============================================================
echo.

REM Test 3: User Registration with Referral Code
echo [3/3] Testing User Registration with Referral Code...
echo.

curl -X POST %API_URL%/register?ref=ABCD1234 ^
  -H "Content-Type: application/json" ^
  -d "{ ^
    \"username\": \"refuser!TIMESTAMP!\", ^
    \"email\": \"refuser!TIMESTAMP!@example.com\", ^
    \"password\": \"TestPassword123\", ^
    \"fullName\": \"Referral User\", ^
    \"course\": \"2\", ^
    \"referralCode\": \"ABCD1234\" ^
  }"

echo.
echo.
echo ============================================================
echo  Test Suite Complete!
echo ============================================================
