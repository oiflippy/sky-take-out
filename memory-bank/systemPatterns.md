# System Patterns *Optional*

This file documents recurring patterns and standards used in the project.
It is optional, but recommended to be updated as the project evolves.
YYYY-MM-DD HH:MM:SS - Log of updates made.

*

## Coding Patterns

*   **Data Transfer Objects (DTOs):** [2025-05-09 20:42:26] - Used to transfer data between layers, particularly from the Controller to the Service layer (e.g., [`EmployeeDTO`](sky-pojo/src/main/java/com/sky/dto/EmployeeDTO.java) in "新增员工" feature). This decouples the service layer from HTTP-specific details and provides a clear contract for data exchange.
*   **Service Layer Business Logic:** [2025-05-09 20:42:26] - The Service layer (e.g., [`EmployeeServiceImpl`](sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java)) encapsulates core business logic. This includes:
    *   Data validation (though not explicitly detailed in the "新增员工" pseudocode, it's a common responsibility).
    *   Conversion between DTOs and Entities.
    *   Setting default values for entities (e.g., default status).
    *   Performing security-related operations (e.g., password encoding).
    *   Populating audit fields (e.g., `createTime`, `updateTime`, `createUser`, `updateUser`).
    *   Coordinating calls to Mapper/Repository interfaces for data persistence.

*   **AOP for Common Field Auto-Filling:** [2025-05-10 20:46:00] - Utilizes Aspect-Oriented Programming to automatically populate common entity fields (e.g., `createTime`, `updateTime`, `createUser`, `updateUser`) before database insertion or update operations. This is achieved through:
    *   A custom annotation ([`@AutoFill`](sky-server/src/main/java/com/sky/annotation/AutoFill.java:15)) to mark Mapper methods requiring this behavior.
    *   An enumeration ([`OperationType`](sky-common/src/main/java/com/sky/enumeration/OperationType.java:6)) to distinguish between `INSERT` and `UPDATE` operations.
    *   An Aspect class ([`AutoFillAspect`](sky-server/src/main/java/com/sky/aspect/AutoFillAspect.java:20)) that intercepts calls to annotated Mapper methods and uses reflection to set the field values.
    This pattern centralizes the logic for handling common fields, reduces boilerplate code in Service and Mapper layers, and improves maintainability.
## Architectural Patterns

*   **Three-Tier Architecture (Controller-Service-Mapper):** [2025-05-09 20:42:26] - The application follows a classic three-tier (or layered) architecture:
    *   **Controller Layer (Presentation):** Handles incoming requests, validates input (often via DTOs), and delegates to the Service layer. Example: [`EmployeeController`](sky-server/src/main/java/com/sky/controller/admin/EmployeeController.java).
    *   **Service Layer (Business Logic):** Contains the core business logic, processes data, and coordinates with the Mapper layer. Example: [`EmployeeServiceImpl`](sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java).
    *   **Mapper Layer (Data Access):** Responsible for interacting with the database, performing CRUD operations. Example: [`EmployeeMapper`](sky-server/src/main/java/com/sky/mapper/EmployeeMapper.java).
    This pattern promotes separation of concerns, modularity, and testability. The "新增员工" feature is a clear example of this pattern in action.

## Testing Patterns

*