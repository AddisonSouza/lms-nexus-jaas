### Requirement: Create classroom
`ADMIN_ORG` and `GESTOR` SHALL be able to create a classroom with name, optional description, academic period, and initial status `ACTIVE`. The system SHALL generate a unique 6-character alphanumeric invite code on creation. The `organization_id` SHALL be extracted from the JWT, never from the request body.

#### Scenario: Successful classroom creation
- **WHEN** an authenticated `ADMIN_ORG` or `GESTOR` submits valid classroom data
- **THEN** the system creates the classroom with `organization_id` from JWT and returns the created resource with a 6-character invite code

#### Scenario: Unauthorized creation attempt
- **WHEN** an authenticated `PROFESSOR` or `ALUNO` attempts to create a classroom
- **THEN** the system returns HTTP 403 Forbidden

#### Scenario: Missing required fields
- **WHEN** the request omits `name` or `academicPeriod`
- **THEN** the system returns HTTP 400 with validation error details

---

### Requirement: Update classroom
`ADMIN_ORG` and `GESTOR` SHALL be able to update an existing classroom's name, description, academic period, and status. A classroom's `organization_id` and `inviteCode` SHALL NOT be changed via update.

#### Scenario: Successful classroom update
- **WHEN** an authorized actor submits valid update data for an existing classroom
- **THEN** the system persists the changes and returns the updated resource

#### Scenario: Archive classroom
- **WHEN** an authorized actor sets `status` to `ARCHIVED`
- **THEN** the system updates the status and no new members may be added

#### Scenario: Update non-existent classroom
- **WHEN** the classroom ID does not exist or belongs to a different organization
- **THEN** the system returns HTTP 404

---

### Requirement: Delete classroom (soft delete)
`ADMIN_ORG` and `GESTOR` SHALL be able to soft-delete a classroom. The system SHALL set `deleted_at` and SHALL NOT physically remove the record.

#### Scenario: Successful soft delete
- **WHEN** an authorized actor deletes a classroom
- **THEN** `deleted_at` is set; the classroom no longer appears in listings; historical data is preserved

#### Scenario: Delete classroom with active members
- **WHEN** the classroom has active members at delete time
- **THEN** the system soft-deletes the classroom and all related `classroom_members` records

---

### Requirement: List classrooms
All authenticated members of an organization SHALL be able to list classrooms. The result set SHALL be filtered by `organization_id` from the JWT. `ADMIN_ORG` and `GESTOR` see all active classrooms in the org. `PROFESSOR` and `ALUNO` see only classrooms they are members of.

#### Scenario: ADMIN_ORG lists classrooms
- **WHEN** an `ADMIN_ORG` calls `GET /classrooms`
- **THEN** the system returns all non-deleted classrooms in their organization

#### Scenario: PROFESSOR lists classrooms
- **WHEN** a `PROFESSOR` calls `GET /classrooms`
- **THEN** the system returns only classrooms where the professor is a member

#### Scenario: ALUNO lists classrooms
- **WHEN** an `ALUNO` calls `GET /classrooms`
- **THEN** the system returns only classrooms where the student is a member

---

### Requirement: Get classroom by ID
Any authenticated organization member SHALL be able to retrieve a classroom by ID, subject to the same visibility rules as listing.

#### Scenario: Authorized access
- **WHEN** a member requests a classroom they have access to
- **THEN** the system returns the full classroom details, subject to the invite code visibility rule below

#### Scenario: Unauthorized access
- **WHEN** a `PROFESSOR` or `ALUNO` requests a classroom they are not a member of
- **THEN** the system returns HTTP 403

---

### Requirement: Invite code visibility
`ADMIN_ORG`, `GESTOR`, and `PROFESSOR` SHALL receive the classroom's invite code in both the listing and the detail response. `ALUNO` SHALL receive `inviteCode: null` — a student joins with a code but never gets it back from the system. The same rule SHALL apply to `GET /classrooms` and `GET /classrooms/{id}`.

#### Scenario: Manager or teacher sees the code
- **WHEN** an `ADMIN_ORG`, `GESTOR`, or `PROFESSOR` lists classrooms or opens a classroom they can access
- **THEN** each classroom carries its 6-character invite code

#### Scenario: Student never sees the code
- **WHEN** an `ALUNO` lists classrooms or opens a classroom they are a member of
- **THEN** every classroom is returned with `inviteCode: null`

#### Scenario: Code shown in the classroom list
- **WHEN** a user who may see the code opens the classroom list in the web app
- **THEN** a "Código" column is rendered with the code and a copy button; for an `ALUNO` the column is not rendered at all
