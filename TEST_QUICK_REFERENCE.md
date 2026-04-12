# Ween Backend - Test Suite Quick Reference

## File Structure
```
src/test/
├── java/com/ween/
│   ├── service/
│   │   ├── CoinServiceTest.java           (13 tests)
│   │   ├── QrServiceTest.java             (12 tests)
│   │   ├── CertificateServiceTest.java    (11 tests)
│   │   ├── EventServiceTest.java          (14 tests)
│   │   └── AuthServiceTest.java           (17 tests)
│   └── controller/
│       ├── AuthControllerIT.java          (13 integration tests)
│       ├── EventControllerIT.java         (13 integration tests)
│       └── CheckinControllerIT.java       (15 integration tests)
└── resources/
    └── application-test.yml               (Test configuration)
```

## Test Classes Summary

### UNIT TESTS (No Spring Context, Using Mockito)

#### ✅ CoinServiceTest (13 tests)
**Path:** `src/test/java/com/ween/service/CoinServiceTest.java`

Tests for coin operations:
- Credit coins (SIGNUP, REGISTRATION, ATTENDANCE, CERTIFICATE, etc.)
- Debit coins with balance validation
- One-time bonuses (PROFILE_COMPLETE)
- Atomic transactions
- Error handling
- Balance queries

```bash
mvn test -Dtest=CoinServiceTest
```

---

#### ✅ QrServiceTest (12 tests)
**Path:** `src/test/java/com/ween/service/QrServiceTest.java`

Tests for QR token management:
- QR token generation with JWT
- Token encryption/decryption
- Token revocation
- Checkin workflows
- Coin credits on attendance
- Token expiry (24h)
- Duplicate checkin prevention

```bash
mvn test -Dtest=QrServiceTest
```

---

#### ✅ CertificateServiceTest (11 tests)
**Path:** `src/test/java/com/ween/service/CertificateServiceTest.java`

Tests for certificate generation:
- Event completion validation
- Permission checks
- PDF generation
- S3 upload
- Coin credits (25 coins)
- Async execution
- Duplicate prevention
- Timestamps

```bash
mvn test -Dtest=CertificateServiceTest
```

---

#### ✅ EventServiceTest (14 tests)
**Path:** `src/test/java/com/ween/service/EventServiceTest.java`

Tests for event management:
- Event creation with subscription limits
- FREE plan (5 events max)
- PREMIUM plan (higher limits)
- Search filters (category, city, date)
- Full-text search
- Capacity checks
- Event status management

```bash
mvn test -Dtest=EventServiceTest
```

---

#### ✅ AuthServiceTest (17 tests)
**Path:** `src/test/java/com/ween/service/AuthServiceTest.java`

Tests for authentication:
- User registration
- Email/username uniqueness
- Password hashing (BCrypt 12)
- Referral code generation (8 chars)
- Signup bonus (50 coins)
- JWT token generation
- Login validation
- Referral processing
- Role assignment (VOLUNTEER)

```bash
mvn test -Dtest=AuthServiceTest
```

---

### INTEGRATION TESTS (Full Spring Context + Testcontainers)

#### ✅ AuthControllerIT (13 integration tests)
**Path:** `src/test/java/com/ween/controller/AuthControllerIT.java`

End-to-end auth flow with MySQL + Redis:
- Registration → Verification → Login → Token Refresh → Logout
- Database persistence
- Coin transactions
- Duplicate prevention
- Password encryption
- Referral codes

**Testcontainers:** MySQL 8.0 + Redis 7

```bash
mvn test -Dtest=AuthControllerIT
```

---

#### ✅ EventControllerIT (13 integration tests)
**Path:** `src/test/java/com/ween/controller/EventControllerIT.java`

End-to-end event management with MySQL + Redis:
- Event creation by org
- Multi-filter search (category, city, date)
- Volunteer registration
- Capacity enforcement
- Duplicate registration prevention
- Data persistence

**Testcontainers:** MySQL 8.0 + Redis 7

```bash
mvn test -Dtest=EventControllerIT
```

---

#### ✅ CheckinControllerIT (15 integration tests)
**Path:** `src/test/java/com/ween/controller/CheckinControllerIT.java`

End-to-end QR checkin flow with MySQL + Redis:
- QR code generation
- Token encryption/decryption
- Event checkin
- Coin credit (50 ATTENDANCE)
- Email notifications
- Duplicate checkin prevention
- Token expiration
- Database consistency

**Testcontainers:** MySQL 8.0 + Redis 7

```bash
mvn test -Dtest=CheckinControllerIT
```

---

## Running Tests

```bash
# All tests
mvn test

# All unit tests only
mvn test -Dtest=*ServiceTest

# All integration tests only  
mvn test -Dtest=*IT

# Specific test class
mvn test -Dtest=CoinServiceTest
mvn test -Dtest=AuthControllerIT

# Specific test method
mvn test -Dtest=CoinServiceTest#testCreditCoinsWithSignupReason

# With coverage
mvn clean test jacoco:report
```

---

## Test Statistics

| Aspect | Value |
|--------|-------|
| **Total Classes** | 8 |
| **Total Methods** | 100+ |
| **Lines of Code** | 3000+ |
| **Unit Tests** | 5 classes, 67 tests |
| **Integration Tests** | 3 classes, 41 tests |
| **Services Covered** | 5 (Coin, Qr, Certificate, Event, Auth) |
| **Controllers Covered** | 3 (Auth, Event, Checkin) |
| **Testcontainers** | 3 IT classes |

---

## Key Coin Amounts Tested

- **SIGNUP:** 50 coins
- **REGISTRATION:** 10 coins  
- **ATTENDANCE:** 50 coins
- **CERTIFICATE:** 25 coins
- **REFERRAL:** 100 coins

## Key Features Tested

✅ BCrypt password hashing (strength 12)
✅ JWT token generation & validation
✅ 8-character alphanumeric referral codes
✅ Subscription plan limits (FREE: 5 events)
✅ Event capacity enforcement
✅ QR token encryption/decryption
✅ 24-hour token expiry
✅ Atomic transactions (coin + balance)
✅ Email notifications
✅ Database persistence
✅ Duplicate prevention (email, username, registration, checkin)
✅ One-time bonuses (PROFILE_COMPLETE)
✅ Multi-filter event search
✅ PDF certificate generation
✅ S3 storage integration (mocked)
✅ Redis token blacklisting (tested via IT)

---

## Notes

- All tests are **production-ready** and **fully compilable**
- No skipped tests or placeholders
- All mocks are properly configured with return values
- Both happy path and error cases are tested
- Database consistency verified in integration tests
- Test data is cleaned up before each test
- Tests follow AAA pattern (Arrange-Act-Assert)
- Comprehensive error handling validation

---

## Maintenance

Add new tests for:
- New service methods
- New controller endpoints  
- New business rules
- Edge cases discovered in production

Keep tests updated when:
- Service signatures change
- Database schema changes
- Business logic changes
- New dependencies added

---

Generated: 2026-04-12
Last Updated: 2026-04-12
