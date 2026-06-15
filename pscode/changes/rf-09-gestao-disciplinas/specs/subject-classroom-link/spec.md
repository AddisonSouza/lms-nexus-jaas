## ADDED Requirements

### Requirement: Link Subject to Classroom

ADMIN_ORG and GESTOR SHALL be able to link a subject to a classroom within the same organization. The link SHALL be recorded in the `subject_classrooms` associative table.

#### Scenario: Successful subject-classroom link

WHEN an ADMIN_ORG sends POST /subjects/{id}/classrooms with a valid `classroomId`
THEN the system SHALL create a record in `subject_classrooms`
AND return 201 Created

#### Scenario: Idempotent link

WHEN an ADMIN_ORG sends POST /subjects/{id}/classrooms with a `classroomId` that is already linked
THEN the system SHALL return 200 OK without creating a duplicate record

#### Scenario: Link to classroom from different organization

WHEN an ADMIN_ORG sends POST /subjects/{id}/classrooms with a `classroomId` belonging to a different organization
THEN the system SHALL return 404 Not Found

#### Scenario: Link to archived classroom

WHEN an ADMIN_ORG sends POST /subjects/{id}/classrooms with an archived classroom's ID
THEN the system SHALL return 422 Unprocessable Entity with an error indicating the classroom is archived

#### Scenario: Link to non-existent classroom

WHEN an ADMIN_ORG sends POST /subjects/{id}/classrooms with a non-existent `classroomId`
THEN the system SHALL return 404 Not Found

#### Scenario: Link with non-existent subject

WHEN an ADMIN_ORG sends POST /subjects/{id}/classrooms where the subject does not exist
THEN the system SHALL return 404 Not Found

#### Scenario: Link by unauthorized role

WHEN a PROFESSOR sends POST /subjects/{id}/classrooms
THEN the system SHALL return 403 Forbidden

---

### Requirement: Unlink Subject from Classroom

ADMIN_ORG and GESTOR SHALL be able to remove the link between a subject and a classroom.

#### Scenario: Successful unlink

WHEN an ADMIN_ORG sends DELETE /subjects/{id}/classrooms/{classroomId}
THEN the system SHALL remove the record from `subject_classrooms`
AND return 204 No Content

#### Scenario: Unlink non-existent link

WHEN an ADMIN_ORG sends DELETE /subjects/{id}/classrooms/{classroomId} where the link does not exist
THEN the system SHALL return 404 Not Found
