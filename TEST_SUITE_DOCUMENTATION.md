# Ween Backend Test Suite - Complete Guide

## Overview
This document describes the 8 production-ready test classes created for the Ween backend covering both unit tests (using Mockito) and integration tests (using Testcontainers).

---

## UNIT TESTS (5 Classes)

### 1. CoinServiceTest
**Location:** `src/test/java/com/ween/service/CoinServiceTest.java`

**Framework:** JUnit 5 + Mockito (No Spring Context)

**Test Coverage:**
- Credit operations with various CoinReason types (SIGNUP, REGISTRATION, ATTENDANCE, CERTIFICATE, PROFILE_COMPLETE)
- Debit operations with balance validation
- PROFILE_COMPLETE one-time bonus enforcement
- Atomic transactions (balance update + transaction creation occur together)
- Error handling for insufficient balance
- Balance retrieval
- Transaction history queries

**Key Test Cases:**
```
testCreditCoinsWithSignupReason()
testCreditCoinsWithRegistrationReason()
testCreditCoinsWithAttendanceReason()
testCreditCoinsWithCertificateReason()
testBalanceUpdateAtomicWithTransaction()
testProfileCompleteBonus()
testDebitCoins()
testDebitWithInsufficientBalance()
testCreditToNonExistentUser()
testCreditCoinsWithRelatedEntity()
testGetUserCoinBalance()
```

**Mocked Dependencies:**
- CoinTransactionRepository
- UserRepository

---

### 2. QrServiceTest
**Location:** `src/test/java/com/ween/service/QrServiceTest.java`

**Framework:** JUnit 5 + Mockito

**Test Coverage:**
- QR token generation with JWT encryption
- JWT payload validation (contains userId, emailHash, iat, exp, platform)
- Token encryption/decryption round-trip
- Token revocation before generating new tokens
- Checkin operations with valid tokens
- ALREADY_CHECKED_IN status prevention
- Coin credit on attendance (50 coins)
- International event bonus handling
- Token expiry validation (24 hours)

**Key Test Cases:**
```
testGenerateQrToken()
testRevokeExistingTokenBeforeGenerating()
testGenerateQrForNonExistentUser()
testGetQrToken()
testDecryptQrToken()
testDecryptInvalidToken()
testCheckinWithValidToken()
testPreventDuplicateCheckin()
testCheckinWithoutRegistration()
testCoinCreditOnInternationalEventAttendance()
testQrTokenContainsRequiredFields()
testTokenExpirySet24Hours()
```

**Mocked Dependencies:**
- QrTokenRepository
- EventRegistrationRepository
- UserRepository
- JwtUtil
- AesUtil
- RegistrationService
- CoinService

---

### 3. CertificateServiceTest
**Location:** `src/test/java/com/ween/service/CertificateServiceTest.java`

**Framework:** JUnit 5 + Mockito

**Test Coverage:**
- Certificate generation for completed events only
- Organizer permission validation
- CERTIFICATE coin credit (25 coins)
- PDF generation and validation
- S3 storage upload verification
- Async CompletableFuture execution
- Event completion status validation
- Duplicate certificate prevention
- Unique certificate number generation

**Key Test Cases:**
```
testGenerateCertificateForCompletedEvent()
testGenerateCertificateForIncompleteEvent()
testGenerateCertificateUserNotFound()
testGenerateCertificateEventNotFound()
testGenerateCertificateAlreadyExists()
testCertificatePdfGeneration()
testCoinCreditAfterGeneration()
testCertificateTemplateUsed()
testCertificateHasIssuedAtTimestamp()
testPdfUploadAndStorage()
testUniqueCertificateNumbers()
```

**Mocked Dependencies:**
- CertificateRepository
- EventRepository
- UserRepository
- StorageService
- CoinService
- NotificationService
- FirebaseService
- CertificateMapper

---

### 4. EventServiceTest
**Location:** `src/test/java/com/ween/service/EventServiceTest.java`

**Framework:** JUnit 5 + Mockito

**Test Coverage:**
- Event creation with subscription limit enforcement
- FREE plan limit (max 5 events)
- PREMIUM plan higher limits
- Event capacity checks during registration
- Search filters: category, city, date range
- FULLTEXT search across title and description
- Combined multi-filter search
- Event status management (DRAFT on creation)
- Online event support
- Event data persistence

**Key Test Cases:**
```
testCreateEvent()
testCreateEventExceedsSubscriptionLimit()
testFreePlanEventLimit()
testPremiumPlanHigherLimit()
testGetEventById()
testGetEventByIdNotFound()
testSearchEventsByCategory()
testSearchEventsByCity()
testSearchEventsByDateRange()
testFulltextSearch()
testCombinedSearch()
testEventCapacityLimit()
testCreateOnlineEvent()
testEventStatusDraftOnCreation()
```

**Mocked Dependencies:**
- EventRepository
- OrganizationService
- StorageService
- EventMapper

---

### 5. AuthServiceTest
**Location:** `src/test/java/com/ween/service/AuthServiceTest.java`

**Framework:** JUnit 5 + Mockito

**Test Coverage:**
- User registration with email/username uniqueness validation
- Password hashing with BCrypt (strength 12)
- 8-character alphanumeric referral code generation
- 50-coin SIGNUP bonus credit
- JWT access token generation with user ID and email
- JWT refresh token generation with user ID
- Login with credentials validation
- Password matching verification
- Welcome email sending
- Referral code processing during registration
- Graceful handling of invalid referral codes
- User role assignment (VOLUNTEER by default)
- Initial coin balance (0)

**Key Test Cases:**
```
testUserRegistration()
testRegistrationWithDuplicateEmail()
testRegistrationWithDuplicateUsername()
testPasswordHashedWithBCrypt()
testReferralCodeGeneration()
testSignupCoinBonus()
testLoginSuccess()
testLoginWithInvalidEmail()
testLoginWithInvalidPassword()
testAccessTokenContainsUserInfo()
testRefreshTokenGeneration()
testDefaultUserRoleVolunteer()
testInitialCoinBalance()
testWelcomeEmailSent()
testReferralCodeProcessing()
testInvalidReferralCodeDoesNotBlockRegistration()
testNullReferralCode()
testUniqueReferralCodes()
```

**Mocked Dependencies:**
- UserRepository
- PasswordEncoder
- JwtUtil
- EmailService
- CoinService

---

## INTEGRATION TESTS (3 Classes)

### 6. AuthControllerIT
**Location:** `src/test/java/com/ween/controller/AuthControllerIT.java`

**Framework:** Spring Boot Test + Testcontainers (MySQL 8.0 + Redis 7)

**Containers:**
- MySQLContainer (MySQL 8.0) - Database persistence
- GenericContainer (Redis 7) - Token/session storage

**Test Coverage:**
- Full end-to-end registration flow
- Email verification integration
- Login with JWT token generation
- Token refresh mechanism
- Logout with token blacklisting
- User data persistence in MySQL
- Coin transaction recording
- Duplicate email/username prevention
- Referral code validation
- Password hashing verification in database
- Unique referral codes per user

**Key Test Cases:**
```
testCompleteRegistrationFlow()
testSignupBonusCredited()
testLoginGeneratesJwtTokens()
testUserDataPersistence()
testDuplicateEmailRejected()
testDuplicateUsernameRejected()
testLoginWithInvalidCredentials()
testLoginWithNonExistentEmail()
testUniqueReferralCodesPerUser()
testTokenRefresh()
testLogout()
testRegistrationWithReferralCode()
testPasswordIsHashedInDatabase()
```

**Endpoints Tested:**
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

---

### 7. EventControllerIT
**Location:** `src/test/java/com/ween/controller/EventControllerIT.java`

**Framework:** Spring Boot Test + Testcontainers (MySQL 8.0 + Redis 7)

**Test Coverage:**
- Event creation by ORGANIZER users
- Search with category filter
- Search with city filter
- Search with date range filter
- Full-text search functionality
- Volunteer registration for events
- Event detail retrieval including participants
- Capacity enforcement (prevent over-registration)
- Duplicate registration prevention
- Event data persistence verification
- Event updates
- 404 handling for non-existent events
- User's registered events list

**Key Test Cases:**
```
testCreateEventAsOrganizer()
testSearchEventsWithFilters()
testSearchEventsByCity()
testSearchEventsByDateRange()
testVolunteerRegistrationForEvent()
testGetEventWithParticipants()
testPreventRegistrationAtCapacity()
testPreventDuplicateRegistration()
testEventDataPersistence()
testUpdateEvent()
testGetNonExistentEvent()
testGetUserRegisteredEvents()
```

**Endpoints Tested:**
- `POST /api/v1/events`
- `GET /api/v1/events/search`
- `GET /api/v1/events/{eventId}`
- `POST /api/v1/events/{eventId}/register`
- `POST /api/v1/events/{eventId}`

---

### 8. CheckinControllerIT
**Location:** `src/test/java/com/ween/controller/CheckinControllerIT.java`

**Framework:** Spring Boot Test + Testcontainers (MySQL 8.0 + Redis 7)

**Test Coverage:**
- QR code generation for volunteers
- QR token encryption/decryption
- Event checkin with token validation
- Coin credit on successful checkin (50 coins for ATTENDANCE)
- Event registration marked as "joined"
- joinedAt timestamp recording
- Duplicate checkin prevention with ALREADY_CHECKED_IN status
- Email notifications on checkin
- QR token expiration handling (24 hours)
- Previous token revocation on new generation
- International event bonus handling
- Database consistency during checkin
- 404 handling for non-existent events/users
- Unregistered user checkin prevention

**Key Test Cases:**
```
testGenerateQrCode()
testDecryptQrToken()
testCheckinAtEvent()
testCoinCreditOnCheckin()
testRegistrationMarkedAsJoined()
testPreventDuplicateCheckin()
testEmailNotificationOnCheckin()
testQrTokenExpiration()
testPreviousTokenRevokedOnNewGeneration()
testAttendanceCoinAmount()
testInternationalEventBonus()
testCheckinNonExistentEvent()
testCheckinWithoutRegistration()
testQrTokenJwtPayload()
testDatabaseConsistencyDuringCheckin()
```

**Endpoints Tested:**
- `POST /api/v1/qr/generate`
- `POST /api/v1/qr/decrypt`
- `POST /api/v1/qr/validate`
- `POST /api/v1/checkin`

---

## Test Execution

### Run All Tests
```bash
mvn test
```

### Run Only Unit Tests
```bash
mvn test -Dtest=*ServiceTest
```

### Run Only Integration Tests
```bash
mvn test -Dtest=*IT
```

### Run Specific Test Class
```bash
mvn test -Dtest=CoinServiceTest
mvn test -Dtest=AuthControllerIT
```

### Run with Coverage Report
```bash
mvn clean test jacoco:report
```

---

## Test Configuration

### Test Profile: application-test.yml
Located at `src/test/resources/application-test.yml`

Configuration includes:
- MySQL test database (ween_test)
- Redis test server
- Flyway migration disabled
- JPA DDL auto: create-drop
- JWT configuration for tests

### Key Test Settings
- Spring Boot Test Environment: RANDOM_PORT
- Testcontainers: Auto-managed lifecycle
- Database: Cleaned before each test via @BeforeEach
- Transactions: Rollback after each test (for integration tests)

---

## Dependencies

All required dependencies are included in pom.xml:

**Testing Frameworks:**
- JUnit 5 (Jupiter)
- Mockito 4.x
- Spring Boot Test
- Spring Security Test

**Testcontainers:**
- testcontainers:1.19.3
- testcontainers-mysql:1.19.3
- testcontainers-generic:1.19.3

---

## Best Practices Implemented

1. **Clear Test Names:** All tests use @DisplayName for clarity
2. **Comprehensive Coverage:** Happy path, error cases, edge cases
3. **AAA Pattern:** All tests follow Arrange-Act-Assert pattern
4. **Mock Verification:** Verify mock interactions with verify()
5. **Test Isolation:** Each test is independent and can run in any order
6. **Data Cleanup:** @BeforeEach methods clean databases
7. **Meaningful Assertions:** Multiple assertions per test validate behavior
8. **No Skipped Tests:** All tests are complete and runnable
9. **Production-Ready:** All code is compilable and follows best practices
10. **Documentation:** Comprehensive JavaDoc and inline comments

---

## Statistics

| Metric | Count |
|--------|-------|
| Total Test Classes | 8 |
| Unit Test Classes | 5 |
| Integration Test Classes | 3 |
| Total Test Methods | 100+ |
| Lines of Test Code | 3000+ |
| Services Tested | 5 |
| Controllers Tested | 3 |
| Test Coverage Areas | Services, Controllers, Persistence, Transactions |

---

## Extending the Tests

To add more tests:

1. **Unit Tests:** Add @Test methods to existing test classes with @Mock/@InjectMocks
2. **Integration Tests:** Add @Test methods to existing IT classes with Testcontainers setup
3. **New Services:** Create new test class following the same pattern
4. **New Controllers:** Create new IT class with Testcontainers

---

## Troubleshooting

### Tests Won't Run
- Ensure test resources folder has application-test.yml
- Verify Testcontainers Docker is available
- Check MySQL and Redis container availability

### Database Issues
- Testcontainers will create containers automatically
- Ensure Docker daemon is running
- Check port availability (3306, 6379)

### Mock Issues
- Verify @Mock annotations on dependencies
- Ensure @InjectMocks annotation on service under test
- Check when() mock setup matches method calls

---

## Maintenance

- Update tests when service signatures change
- Add tests for new features before implementation (TDD)
- Review test coverage periodically
- Keep Testcontainers version updated with Spring Boot upgrades
