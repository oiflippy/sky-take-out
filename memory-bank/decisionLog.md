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
---
### Decision (Debug)
[2025-05-09 21:01:38] - Bug Fix Strategy: Corrected Mockito verification in `EmployeeServiceImplTest.testBatchSaveEmployees`.

**Rationale:**
The test failed with `org.mockito.exceptions.verification.TooManyActualInvocations` because `Mockito.verify` was called inside a loop, leading to an incorrect verification count after the first iteration. The fix involves moving the `verify` call outside the loop and using `Mockito.times(50)` to assert the `insert` method on the mock `employeeMapper` was called the expected number of times (50) throughout the loop's execution.

**Details:**
Affected components/files:
- Test File: [`sky-server/src/test/java/com/sky/service/impl/EmployeeServiceImplTest.java`](sky-server/src/test/java/com/sky/service/impl/EmployeeServiceImplTest.java)
- Method: `testBatchSaveEmployees()`
---
### Decision (Code)
[2025-05-09 21:21:15] - 将员工测试数据初始化逻辑从 `CommandLineRunner` 迁移到专用的测试类 `TestDataInitializerTest.java`。

**Rationale:**
根据用户反馈，测试数据的初始化更适合放在测试环境中执行，而不是作为应用启动时的一部分。这样可以更好地隔离测试数据与生产环境，并且方便按需执行数据初始化。

**Details:**
*   新文件: [`sky-server/src/test/java/com/sky/runner/TestDataInitializerTest.java`](sky-server/src/test/java/com/sky/runner/TestDataInitializerTest.java)
*   旧文件 (已删除): `sky-server/src/main/java/com/sky/runner/TestDataInitializer.java`
*   实现方式：使用 `@SpringBootTest` 和 `@Test` 注解，通过 JUnit 执行。

---
### Decision (Architect)
[2025-05-10 13:52:00] - Enhance Employee Update Logic for Username Uniqueness.

**Rationale:**
The current employee update functionality (`EmployeeServiceImpl.update`) lacks proper username uniqueness validation. It does not check if the new username is already taken by *another* employee, excluding the current one being edited. This can lead to data integrity issues or user-facing errors like "'testuser15'已存在" when the system incorrectly flags the current user's existing username as a duplicate during an update attempt without changing the username, or when changing to a username already in use by a different employee. The `spec-pseudocode` mode correctly identified this gap.

**Implications/Details:**
*   **Service Layer (`EmployeeServiceImpl.java`):**
    *   The `update` method needs to incorporate a check for username existence, excluding the ID of the employee being updated.
    *   If a username conflict is detected (i.e., another employee already has this username), a `BusinessException` (or a more specific custom exception like `UsernameAlreadyExistsException`) should be thrown with a clear message (e.g., [`MessageConstant.ACCOUNT_ALREADY_EXISTS`](sky-common/src/main/java/com/sky/constant/MessageConstant.java) or a new constant).
*   **Mapper Layer (`EmployeeMapper.java` and `EmployeeMapper.xml`):**
    *   A new method, such as `getByUsernameAndNotId(String username, Long id)`, needs to be added to [`EmployeeMapper.java`](sky-server/src/main/java/com/sky/mapper/EmployeeMapper.java).
    *   The corresponding SQL query in [`EmployeeMapper.xml`](sky-server/src/main/resources/mapper/EmployeeMapper.xml) should select an employee where `username = #{username}` AND `id != #{id}`.
*   **Controller Layer (`EmployeeController.java`):**
    *   No direct changes are likely needed in the controller, as the exception thrown by the service layer should be handled by the global exception handler ([`GlobalExceptionHandler.java`](sky-server/src/main/java/com/sky/handler/GlobalExceptionHandler.java)).
*   **Error Message:** The error message "'testuser15'已存在" suggests the current check might be using `getByUsername` without excluding the current employee's ID, or a database unique constraint is being hit without a prior application-level check. The proposed solution addresses the application-level check.

---
### Decision (Architect)
[2025-05-10 16:06:39] - Implement "分类管理" (Category Management) Feature (Chapter 5.2)

**Rationale:**
To introduce functionality for managing product categories, including creation, reading, updating, deleting (CRUD), and listing categories. This is a core feature for organizing dishes and setmeals within the application. The implementation follows the established three-tier architecture.

**Implications/Details:**
*   **New Components:**
   *   **Mapper Interfaces:**
       *   `com.sky.mapper.DishMapper`: Added `Integer countByCategoryId(Long categoryId)`
       *   `com.sky.mapper.SetmealMapper`: Added `Integer countByCategoryId(Long categoryId)`
       *   `com.sky.mapper.CategoryMapper`: New interface with methods for `insert`, `pageQuery`, `deleteById`, `update`, `list`.
   *   **Mapper XML:**
       *   `sky-server/src/main/resources/mapper/CategoryMapper.xml`: New XML file defining SQL for `CategoryMapper` methods (`pageQuery`, `update`, `list`).
   *   **Service Interface:**
       *   `com.sky.service.CategoryService`: New interface defining business logic methods: `save`, `pageQuery`, `deleteById`, `update`, `startOrStop`, `list`.
   *   **Service Implementation:**
       *   `com.sky.service.impl.CategoryServiceImpl`: New class implementing `CategoryService`.
           *   Injects `CategoryMapper`, `DishMapper`, `SetmealMapper`.
           *   Handles business logic, including checking for associated dishes/setmeals before deletion.
   *   **Controller:**
       *   `com.sky.controller.admin.CategoryController`: New REST controller for admin-side category management.
           *   Maps HTTP requests to `CategoryService` methods.
           *   Endpoints: `/admin/category` (POST for save, PUT for update, DELETE for deleteById), `/admin/category/page` (GET for pageQuery), `/admin/category/status/{status}` (POST for startOrStop), `/admin/category/list` (GET for list).
*   **Key Architectural Points:**
   *   Adherence to the existing **Three-Tier Architecture (Controller-Service-Mapper)**.
   *   Usage of DTOs (`CategoryDTO`, `CategoryPageQueryDTO`) for data transfer between Controller and Service.
   *   `PageHelper` for pagination in the Mapper layer.
   *   Exception handling for deletion constraints (e.g., `DeletionNotAllowedException`).
   *   Use of `BaseContext.getCurrentId()` for `createUser` and `updateUser` fields.
   *   Logging with `@Slf4j`.
   *   Swagger annotations (`@Api`, `@ApiOperation`) for API documentation.
*   **Dependencies:**
   *   `CategoryServiceImpl` depends on `CategoryMapper`, `DishMapper`, and `SetmealMapper`.
   *   `CategoryController` depends on `CategoryService`.
*   **Noteworthy Implementation Details:**
   *   `CategoryServiceImpl.deleteById` checks `DishMapper` and `SetmealMapper` before deleting a category to prevent data integrity issues.
   *   `CategoryServiceImpl.save` sets default status and audit fields.
   *   `CategoryMapper.xml` includes dynamic SQL for filtering and ordering.
---
### Decision (Code)
[2025-05-10 16:09:22] - Created `DishMapper.java` interface with `countByCategoryId` method.

**Rationale:**
This mapper interface and method are required for the "分类管理" (Category Management) feature. Specifically, it's used by `CategoryServiceImpl` to check if a category is associated with any dishes before allowing its deletion, thus ensuring data integrity.

**Details:**
*   File Created: [`sky-server/src/main/java/com/sky/mapper/DishMapper.java`](sky-server/src/main/java/com/sky/mapper/DishMapper.java)
*   Method Added: `Integer countByCategoryId(Long categoryId)`

---
### Decision (Code)
[2025-05-10 16:10:50] - Created `SetmealMapper.java` interface with `countByCategoryId` method.

**Rationale:**
This mapper interface and method are required for the "分类管理" (Category Management) feature. Specifically, it's used by `CategoryServiceImpl` to check if a category is associated with any setmeals before allowing its deletion, thus ensuring data integrity.

**Details:**
*   File Created: [`sky-server/src/main/java/com/sky/mapper/SetmealMapper.java`](sky-server/src/main/java/com/sky/mapper/SetmealMapper.java)
*   Method Added: `Integer countByCategoryId(Long categoryId)`
---
### Decision (Code)
[2025-05-10 16:13:28] - Created/Updated `CategoryMapper.java` interface.

**Rationale:**
This mapper interface is a core component for the "分类管理" (Category Management) feature. It defines the data access methods required for category CRUD operations and listing. The provided existing content matched the pseudocode, so it was used directly.

**Details:**
*   File Created/Updated: [`sky-server/src/main/java/com/sky/mapper/CategoryMapper.java`](sky-server/src/main/java/com/sky/mapper/CategoryMapper.java)
*   Methods included: `insert`, `pageQuery`, `deleteById`, `update`, `list`.
---
### Decision (Code)
[2025-05-10 16:15:30] - Created `CategoryMapper.xml` file.

**Rationale:**
This XML mapper file is essential for the "分类管理" (Category Management) feature. It defines the SQL queries corresponding to the methods in the `CategoryMapper.java` interface, enabling database interactions for category CRUD operations and listing. The content was generated based on the provided pseudocode, ensuring adherence to MyBatis conventions and project-specific requirements.

**Details:**
*   File Created: [`sky-server/src/main/resources/mapper/CategoryMapper.xml`](sky-server/src/main/resources/mapper/CategoryMapper.xml)
*   Key elements: `mapper` namespace, `select` for `pageQuery` and `list`, `update` for `update`.
---
### Decision (Code)
[2025-05-10 16:17:01] - Created/Updated `CategoryService.java` interface.

**Rationale:**
This service interface is a core component for the "分类管理" (Category Management) feature. It defines the business logic methods required for category operations. The content was based on the provided pseudocode and reference material.

**Details:**
*   File Created/Updated: [`sky-server/src/main/java/com/sky/service/CategoryService.java`](sky-server/src/main/java/com/sky/service/CategoryService.java)
*   Methods included: `save`, `pageQuery`, `deleteById`, `update`, `startOrStop`, `list`.

---
### Decision (Code)
[2025-05-10 16:19:36] - Created `CategoryServiceImpl.java` class.

**Rationale:**
This service implementation is a core component for the "分类管理" (Category Management) feature. It implements the `CategoryService` interface and contains the business logic for category operations, such as creating, updating, deleting, and querying categories. It also handles dependencies like `CategoryMapper`, `DishMapper`, and `SetmealMapper` for data access and validation.

**Details:**
*   File Created: [`sky-server/src/main/java/com/sky/service/impl/CategoryServiceImpl.java`](sky-server/src/main/java/com/sky/service/impl/CategoryServiceImpl.java)
*   Key methods implemented: `save`, `pageQuery`, `deleteById`, `update`, `startOrStop`, `list`.
*   Dependencies Injected: `CategoryMapper`, `DishMapper`, `SetmealMapper`.
*   Business Logic: Includes setting default status and audit fields on save, checking for related dishes/setmeals before deletion.
---
### Decision (Code)
[2025-05-10 16:23:33] - Created `CategoryController.java` class.

**Rationale:**
This controller is a core component for the "分类管理" (Category Management) feature. It handles HTTP requests related to category management and delegates to the `CategoryService`. The implementation is based on the provided pseudocode and aligns with the project's RESTful API design.

**Details:**
*   File Created: [`sky-server/src/main/java/com/sky/controller/admin/CategoryController.java`](sky-server/src/main/java/com/sky/controller/admin/CategoryController.java)
*   Key methods implemented: `save`, `page`, `deleteById`, `update`, `startOrStop`, `list`.
*   Dependencies Injected: `CategoryService`.
*   Annotations: `@RestController`, `@RequestMapping`, `@Api`, `@ApiOperation`, `@Slf4j`.