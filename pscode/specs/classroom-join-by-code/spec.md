### Requirement: Join classroom by invite code
An authenticated user with role ALUNO SHALL be able to join a classroom by submitting a 6-character invite code. The system SHALL create a `classroom_members` record with `role=ALUNO` and the `organization_id` of the classroom.

#### Scenario: Successful join
- **WHEN** an ALUNO submits a valid invite code for an ACTIVE classroom
- **THEN** the system creates a `classroom_members` entry with `role=ALUNO` and returns 201 with the classroom data

#### Scenario: Idempotent join
- **WHEN** an ALUNO submits a valid invite code for a classroom they already belong to
- **THEN** the system returns 200 with the classroom data without creating a duplicate member record

#### Scenario: Invalid invite code
- **WHEN** a user submits a code that does not match any classroom
- **THEN** the system returns 404 with a clear error message

#### Scenario: Archived classroom blocked
- **WHEN** a user submits a valid code for a classroom with status ARCHIVED
- **THEN** the system returns 409 with an error indicating the classroom is archived and join is not allowed

---

### Requirement: Regenerate invite code
A user with role PROFESSOR, GESTOR, or ADMIN_ORG SHALL be able to regenerate the invite code for a classroom they are associated with. The old code SHALL be immediately invalidated.

#### Scenario: Successful regeneration
- **WHEN** a PROFESSOR or GESTOR requests regeneration for an ACTIVE classroom
- **THEN** the system generates a new 6-character code, persists it, and returns 200 with the updated classroom data including the new code

#### Scenario: Regeneration blocked for ALUNO
- **WHEN** an ALUNO requests invite code regeneration
- **THEN** the system returns 403 Forbidden

#### Scenario: Regeneration on archived classroom
- **WHEN** regeneration is requested for an ARCHIVED classroom
- **THEN** the system returns 409 indicating the classroom is archived
