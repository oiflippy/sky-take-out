---
### Decision (Debug)
[2025-05-09 08:50:06] - Bug Fix Strategy: Modify `employee` table's `status` column from `INT` with length `0` to `TINYINT` or standard `INT`.

**Rationale:**
The `java.sql.SQLException: Data truncated for column 'status' at row 1` error occurs because the `status` column, defined as `INT` with a length/display width of `0`, cannot store the integer value `1`. Standard integer types like `TINYINT` or a correctly defined `INT` will resolve this by providing adequate storage capacity. `TINYINT` is preferred for boolean-like status flags due to its smaller storage footprint.

**Details:**
Affected components/files:
- Database table: `employee`
- Column: `status`

---
### Decision (Debug)
[2025-05-09 09:24:18] - Bug Fix Strategy: Corrected column order in MyBatis `@Insert` annotation in `EmployeeMapper.java` by explicitly listing column names.

**Rationale:**
The persistent `java.sql.SQLException: Data truncated for column 'status' at row 1` error, even after correcting the `status` column type in the database, was due to an incorrect mapping of parameters in the `INSERT` statement. The statement did not explicitly name columns, leading to `createTime` (a datetime value) being incorrectly mapped to the `status` (an INT) column due to their relative positions in the `VALUES` clause versus the table definition. Explicitly naming columns in the `INSERT` statement ensures correct parameter-to-column mapping.

**Details:**
Affected components/files:
- Java Mapper: [`com.sky.mapper.EmployeeMapper.java`](sky-server/src/main/java/com/sky/mapper/EmployeeMapper.java)
- Annotation: `@Insert` for the `insert` method.
---
### Decision
[2025-05-09 11:11:52] - Refactor `EmployeeServiceImpl.save()` to use `BaseContext.getCurrentId()` for `createUser` and `updateUser` fields.

**Rationale:**
To accurately record the user ID performing the entity creation and initial update, replacing hardcoded placeholder values (`10L`). This change enhances data integrity, auditability, and aligns with best practices for tracking data provenance. It leverages the existing `BaseContext` utility designed for retrieving contextual user information.

**Implications/Details:**
*   **File Affected:** [`sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java`](sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java)
*   **Method Affected:** `save(EmployeeDTO employeeDTO)`
*   **New Import Required:** `com.sky.context.BaseContext`
*   **Logic Change:**
    *   Retrieve `Long currentUserId = BaseContext.getCurrentId();`
    *   Set `employee.setCreateUser(currentUserId);`
    *   Set `employee.setUpdateUser(currentUserId);`
*   **Assumed Behavior:** `BaseContext.getCurrentId()` correctly returns the ID of the currently authenticated and authorized user performing the operation.
---
### Decision (Code)
[2025-05-09 11:29:34] - Fixed ThreadLocal Leak in JwtAuthenticationTokenFilter

**Rationale:**
To prevent potential issues in thread-pooled environments where a previous user's ID might be incorrectly reused by a subsequent request. Ensured `BaseContext.removeCurrentId()` is called in a `finally` block within the `doFilterInternal` method. This guarantees cleanup even if exceptions occur during filter chain processing.

**Details:**
*   **File Affected:** [`sky-server/src/main/java/com/sky/config/JwtAuthenticationTokenFilter.java`](sky-server/src/main/java/com/sky/config/JwtAuthenticationTokenFilter.java)
*   **Method Affected:** `doFilterInternal`
*   **Logic Change:** Wrapped `filterChain.doFilter(request, response);` in a `try...finally` block. Added `BaseContext.removeCurrentId();` and a debug log statement in the `finally` block.