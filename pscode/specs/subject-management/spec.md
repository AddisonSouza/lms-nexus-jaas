### Requirement: Create Subject

ADMIN_ORG and GESTOR SHALL be able to create a subject (disciplina) within their organization, providing name, optional code, optional description, and optional workload hours. The subject SHALL be scoped to the `organization_id` extracted from the JWT.

#### Scenario: Successful subject creation

WHEN an ADMIN_ORG sends POST /subjects with a valid name
THEN the system SHALL create the subject with the correct `organization_id` from JWT
AND return 201 Created with the subject representation including generated `id`

#### Scenario: Subject creation without optional fields

WHEN an ADMIN_ORG sends POST /subjects with only `name`
THEN the system SHALL create the subject with `code`, `description`, and `workloadHours` as null
AND return 201 Created

#### Scenario: Subject creation by unauthorized role

WHEN a PROFESSOR or ALUNO sends POST /subjects
THEN the system SHALL return 403 Forbidden

#### Scenario: Subject creation with missing name

WHEN an ADMIN_ORG sends POST /subjects without `name`
THEN the system SHALL return 400 Bad Request with a validation error message

---

### Requirement: List Subjects

ADMIN_ORG, GESTOR, and PROFESSOR SHALL be able to list all non-deleted subjects belonging to their organization. The list SHALL be filtered by `organization_id` from the JWT.

#### Scenario: List subjects with results

WHEN an ADMIN_ORG sends GET /subjects
THEN the system SHALL return 200 OK with a list of subjects belonging to the organization
AND soft-deleted subjects SHALL NOT appear

#### Scenario: List subjects for empty organization

WHEN an ADMIN_ORG sends GET /subjects and no subjects exist yet
THEN the system SHALL return 200 OK with an empty list

#### Scenario: PROFESSOR lists subjects

WHEN a PROFESSOR sends GET /subjects
THEN the system SHALL return 200 OK with all non-deleted subjects in the organization

---

### Requirement: Get Subject

ADMIN_ORG, GESTOR, and PROFESSOR SHALL be able to retrieve the full details of a single subject by ID, including its linked classrooms and teachers.

#### Scenario: Successful subject retrieval

WHEN an ADMIN_ORG sends GET /subjects/{id} for an existing subject in their organization
THEN the system SHALL return 200 OK with the full subject representation

#### Scenario: Subject not found

WHEN any authorized user sends GET /subjects/{id} for a non-existent or deleted subject
THEN the system SHALL return 404 Not Found

#### Scenario: Cross-organization access

WHEN an ADMIN_ORG sends GET /subjects/{id} for a subject belonging to a different organization
THEN the system SHALL return 404 Not Found (no information leakage)

---

### Requirement: Update Subject

ADMIN_ORG and GESTOR SHALL be able to update a subject's name, code, description, and workload hours.

#### Scenario: Successful subject update

WHEN an ADMIN_ORG sends PUT /subjects/{id} with valid fields
THEN the system SHALL update the subject fields
AND return 200 OK with the updated subject representation

#### Scenario: Update non-existent subject

WHEN an ADMIN_ORG sends PUT /subjects/{id} for a non-existent or deleted subject
THEN the system SHALL return 404 Not Found

#### Scenario: Update by unauthorized role

WHEN a PROFESSOR sends PUT /subjects/{id}
THEN the system SHALL return 403 Forbidden

---

### Requirement: Delete Subject (Soft Delete)

ADMIN_ORG SHALL be able to soft-delete a subject by setting `deleted_at`. Soft-deleted subjects SHALL NOT appear in listings or GET by ID.

#### Scenario: Successful soft delete

WHEN an ADMIN_ORG sends DELETE /subjects/{id}
THEN the system SHALL set `deleted_at` to the current timestamp
AND return 204 No Content
AND the subject SHALL no longer appear in GET /subjects or GET /subjects/{id}

#### Scenario: Delete non-existent or already deleted subject

WHEN an ADMIN_ORG sends DELETE /subjects/{id} for a non-existent or already-deleted subject
THEN the system SHALL return 404 Not Found

#### Scenario: Delete by unauthorized role

WHEN a GESTOR or PROFESSOR sends DELETE /subjects/{id}
THEN the system SHALL return 403 Forbidden

---

### Requirement: Subject list reports load failures
The subject list SHALL distinguish a failed request from an organization with no subjects. The empty state SHALL be shown only when the request succeeded and returned no subject.

#### Scenario: Request fails
- **WHEN** `GET /subjects` fails for any reason — an expired session, a token without the `org` claim (HTTP 403), or a server error
- **THEN** the list renders a failure message naming what could not be loaded, plus a "Tentar de novo" action, and renders no table and no empty-state message

#### Scenario: User retries
- **WHEN** the user activates "Tentar de novo" and the request then succeeds
- **THEN** the list renders the subjects; while the retry is in flight the action is disabled

#### Scenario: Organization genuinely has no subject
- **WHEN** `GET /subjects` succeeds and returns an empty list
- **THEN** the list renders "Nenhuma disciplina encontrada."
