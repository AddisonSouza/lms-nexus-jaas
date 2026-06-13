### Requirement: Add member to classroom
`ADMIN_ORG` and `GESTOR` SHALL be able to add an existing organization member to a classroom with role `PROFESSOR` or `ALUNO`. The operation SHALL be idempotent — adding an already-existing member SHALL NOT create a duplicate entry.

#### Scenario: Successful member addition
- **WHEN** an authorized actor adds a valid organization member with a valid role
- **THEN** a `classroom_members` entry is created (or the existing one is preserved if already present)

#### Scenario: Add non-organization member
- **WHEN** the target user is not a member of the classroom's organization
- **THEN** the system returns HTTP 422 with an appropriate error message

#### Scenario: Idempotent add
- **WHEN** the same user is added to the same classroom again with the same role
- **THEN** the system returns HTTP 200 without creating a duplicate entry

#### Scenario: Add member to archived classroom
- **WHEN** an authorized actor attempts to add a member to an `ARCHIVED` classroom
- **THEN** the system returns HTTP 422

---

### Requirement: Remove member from classroom
`ADMIN_ORG` and `GESTOR` SHALL be able to remove a member from a classroom (soft delete on `classroom_members`).

#### Scenario: Successful member removal
- **WHEN** an authorized actor removes a member from a classroom
- **THEN** the `classroom_members` record has `deleted_at` set; the member no longer appears in classroom membership listings

#### Scenario: Remove non-existent membership
- **WHEN** the target user is not a member of the classroom
- **THEN** the system returns HTTP 404

---

### Requirement: List classroom members
Any organization member SHALL be able to list members of a classroom they have access to.

#### Scenario: List members
- **WHEN** an authorized member calls `GET /classrooms/{id}/members`
- **THEN** the system returns all active members of the classroom with their roles
