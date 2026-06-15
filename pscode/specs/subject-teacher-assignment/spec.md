### Requirement: Assign Teacher to Subject

ADMIN_ORG and GESTOR SHALL be able to assign a teacher (organization member with PROFESSOR role) to a subject. The assignment is at the subject level within the organization (RN-06), recorded in `subject_teachers`.

#### Scenario: Successful teacher assignment

WHEN an ADMIN_ORG sends POST /subjects/{id}/teachers with a valid `memberId` of a PROFESSOR in the organization
THEN the system SHALL create a record in `subject_teachers`
AND return 201 Created

#### Scenario: Idempotent assignment

WHEN an ADMIN_ORG sends POST /subjects/{id}/teachers with a `memberId` already assigned
THEN the system SHALL return 200 OK without creating a duplicate record

#### Scenario: Assign non-PROFESSOR member

WHEN an ADMIN_ORG sends POST /subjects/{id}/teachers with a `memberId` whose role is not PROFESSOR
THEN the system SHALL return 422 Unprocessable Entity with a validation error

#### Scenario: Assign member from different organization

WHEN an ADMIN_ORG sends POST /subjects/{id}/teachers with a `memberId` not belonging to the organization
THEN the system SHALL return 404 Not Found

#### Scenario: Assign to non-existent subject

WHEN an ADMIN_ORG sends POST /subjects/{id}/teachers where the subject does not exist
THEN the system SHALL return 404 Not Found

#### Scenario: Assign by unauthorized role

WHEN a PROFESSOR or ALUNO sends POST /subjects/{id}/teachers
THEN the system SHALL return 403 Forbidden

#### Scenario: Professor assigned to multiple subjects (RN-06)

WHEN a PROFESSOR is already assigned to one subject and an ADMIN_ORG assigns the same PROFESSOR to a different subject
THEN the system SHALL successfully create both assignments
AND return 201 Created for the second assignment

---

### Requirement: Remove Teacher from Subject

ADMIN_ORG and GESTOR SHALL be able to remove a teacher assignment from a subject.

#### Scenario: Successful removal

WHEN an ADMIN_ORG sends DELETE /subjects/{id}/teachers/{memberId}
THEN the system SHALL remove the record from `subject_teachers`
AND return 204 No Content

#### Scenario: Remove non-existent assignment

WHEN an ADMIN_ORG sends DELETE /subjects/{id}/teachers/{memberId} where the assignment does not exist
THEN the system SHALL return 404 Not Found
