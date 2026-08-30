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

---

### Requirement: Invite code is scoped to the organization
A classroom invite code SHALL only resolve inside the organization of the JWT. A user SHALL NOT join a classroom of an organization they do not belong to, and the response SHALL NOT reveal that such a classroom exists.

#### Scenario: Code belongs to another organization
- **WHEN** an authenticated user posts a code that is valid in a different organization
- **THEN** the system returns `404 INVALID_INVITE_CODE` and creates no `classroom_members` row

#### Scenario: Code belongs to the user's organization
- **WHEN** an authenticated user posts a code from their own organization
- **THEN** the user joins as `ALUNO` and the system returns 201

---

### Requirement: Joining never returns the invite code
The join response SHALL carry `inviteCode: null`. Whoever joins by code joins as `ALUNO`, and an `ALUNO` never receives the code back — the same rule the listing and detail responses already follow.

#### Scenario: Code hidden on a fresh join
- **WHEN** a user joins a classroom by code
- **THEN** the returned classroom carries `inviteCode: null`

#### Scenario: Code hidden when repeating the join
- **WHEN** a user posts the code of a classroom they already belong to
- **THEN** the system returns 200, the returned classroom carries `inviteCode: null`, and no duplicate membership is created

---

### Requirement: Code is matched regardless of typed case
The code SHALL be trimmed and upper-cased before validation, so a correct code typed in lower case is accepted rather than rejected by the form.

#### Scenario: Student types the code in lower case
- **WHEN** a student types a valid code in lower case and submits
- **THEN** the code is normalized to upper case and the join proceeds
