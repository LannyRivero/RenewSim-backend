# TECH DEBT: 72 Pre-existing Test Failures

## Status
Tracked (not blocking)

## Impact
- 17 failures
- 55 errors
- Total: 72/358 tests failing (20%)

## Root Causes
1. Spring context config issues (25 tests)
2. Uninitialized mocks (15 tests)
3. Invalid test data (10 tests)
4. Entity mapping assumptions (5 tests)
5. Integration test infrastructure (17 tests)

## Priority
P2 - Fix incrementally, does not block production deployment

## Actions
- [ ] Fix Spring context loading
- [ ] Initialize mocks properly
- [ ] Update test data validity
- [ ] Align entity expectations with domain

## Note
These failures PRE-DATE the RoleName extraction (Task 1.2).
Confirmed by: zero import/compilation errors related to RoleName.
