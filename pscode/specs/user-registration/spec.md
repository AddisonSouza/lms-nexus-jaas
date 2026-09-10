## Requirements

### Requirement: User can register an account
The system SHALL allow any unauthenticated person to create an account by providing full name, e-mail, and password. The account is created with status `PENDING_CONFIRMATION` and a confirmation e-mail is sent. The account SHALL NOT be usable for login until confirmed.

#### Scenario: Successful registration
- **WHEN** user submits valid full name, unique e-mail, and a strong password confirmed in a second field
- **THEN** system creates the user with status `PENDING_CONFIRMATION`
- **AND** system sends a confirmation e-mail containing a token valid for 24 hours
- **AND** system returns `201 Created`
- **AND** user is redirected to a page instructing them to confirm their e-mail

#### Scenario: Duplicate e-mail
- **WHEN** user submits an e-mail already registered in the system
- **THEN** system returns `409 Conflict` with message "E-mail já em uso"
- **AND** no new user is created

#### Scenario: Weak password
- **WHEN** user submits a password with fewer than 8 characters
- **THEN** system returns `422 Unprocessable Entity` with a validation message
- **AND** no user is created

#### Scenario: Missing required fields
- **WHEN** user submits the form with any required field empty (full name, e-mail, or password)
- **THEN** system returns `422 Unprocessable Entity` listing the missing fields
- **AND** no user is created

### Requirement: Registration form enforces a strong, confirmed password
The registration form SHALL require the password twice and SHALL reject, before any request is sent, a password that does not have at least 8 characters with an uppercase letter, a lowercase letter, a digit and a symbol. The error message SHALL name only the criteria that are missing.

#### Scenario: Password missing strength criteria
- **WHEN** user submits a password that meets the minimum length but lacks one or more character classes
- **THEN** the form shows a single message listing the missing criteria (e.g. "A senha precisa de: uma maiúscula, um número")
- **AND** no request is sent to the server

#### Scenario: Password shorter than the minimum
- **WHEN** user submits a password with fewer than 8 characters
- **THEN** the form shows "Senha deve ter no mínimo 8 caracteres"
- **AND** no request is sent to the server

#### Scenario: Confirmation does not match
- **WHEN** user submits a password and a confirmation that differ
- **THEN** the form shows "As senhas não conferem" under the confirmation field
- **AND** no request is sent to the server

#### Scenario: Confirmation is not sent to the server
- **WHEN** the form submits a valid registration
- **THEN** the request body carries only full name, e-mail and password

### Requirement: Password is stored securely
The system SHALL store passwords using BCrypt with a minimum cost factor of 12. Plain-text passwords SHALL never be persisted or logged.

#### Scenario: Password hashing on registration
- **WHEN** a new user is created
- **THEN** the stored password hash SHALL be a BCrypt hash with cost factor >= 12
- **AND** the plain-text password SHALL NOT appear in any database column or application log

### Requirement: Confirmation token expires
The e-mail confirmation token SHALL expire 24 hours after issuance. After expiry, the user SHALL be able to request a new confirmation e-mail.

#### Scenario: Token expiry
- **WHEN** 24 hours have passed since registration
- **THEN** the confirmation token is no longer valid
- **AND** the user's status remains `PENDING_CONFIRMATION`

#### Scenario: Resend confirmation e-mail
- **WHEN** user requests a new confirmation e-mail with a valid (registered) e-mail address
- **THEN** system issues a new token and sends a new confirmation e-mail
- **AND** any previous token for that user is invalidated

### Requirement: Login blocked until confirmation
The system SHALL prevent users with status `PENDING_CONFIRMATION` from authenticating.

#### Scenario: Login attempt before confirmation
- **WHEN** a user with status `PENDING_CONFIRMATION` attempts to log in
- **THEN** system returns `403 Forbidden` with message "Confirme seu e-mail antes de fazer login"
