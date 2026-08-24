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

### Requirement: Immutable invite code
A classroom's invite code SHALL be generated once at creation and SHALL never change. The system SHALL expose no endpoint to regenerate it, and updating a classroom SHALL preserve the existing code.

#### Scenario: Code survives a classroom update
- **WHEN** an `ADMIN_ORG` or `GESTOR` updates a classroom's name, description, academic period, or status
- **THEN** the classroom keeps the invite code it was created with

#### Scenario: No regeneration endpoint
- **WHEN** any actor calls `POST /classrooms/{id}/invite-code/regenerate`
- **THEN** the system returns 404, as the endpoint does not exist
