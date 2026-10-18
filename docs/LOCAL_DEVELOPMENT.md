# Local Development

Run PostgreSQL, the backend, and the frontend in three separate PowerShell terminals.

## 1. Check PostgreSQL

Confirm the PostgreSQL port is reachable:

```powershell
Test-NetConnection localhost -Port 5432
```

Then verify that the project database and user can connect:

```powershell
psql -h localhost -p 5432 -U civic_app -d civic_transparency -c "SELECT current_database(), current_user;"
```

Enter the `civic_app` password when prompted. A result showing `civic_transparency` and `civic_app` confirms the database connection works.

## 2. Start the backend

The backend expects its local database password and signing/encryption keys in the ignored local config file `backend/application-local.properties`. Keep that file local; do not commit or share its secrets.

In a new PowerShell terminal:

```powershell
cd C:\Kareem\Code\CivicTransparency\backend
$env:SPRING_CONFIG_IMPORT = 'optional:file:./application-local.properties'
.\gradlew.bat bootRun
```

Leave this terminal running. A successful startup log should say that Tomcat started on port `8080`. If startup fails, check the first `Caused by` message; a missing local config or incorrect database password can prevent startup.

## 3. Start the frontend

In another PowerShell terminal:

```powershell
cd C:\Kareem\Code\CivicTransparency\frontend
npm run dev
```

If dependencies are not installed yet, run `npm install --no-package-lock` in the frontend folder first, then run `npm run dev` again. Leave this terminal running.

Open [http://localhost:3000](http://localhost:3000). The frontend sends API requests to the backend at `http://localhost:8080`.

## 4. Test the authority workflow and optional email

After a `SYSTEM_ADMIN` has been provisioned through the secure bootstrap process, use [http://localhost:3000/authority/login](http://localhost:3000/authority/login) to sign in. System administrators are directed to `/admin`, where they can create authorities, departments, authority staff accounts, and routing rules. The first system administrator cannot be self-registered through the public site.

Authority staff accounts must be provisioned by a system administrator; there is no public authority registration. Staff can sign in at [http://localhost:3000/authority/login](http://localhost:3000/authority/login). Authority admins and officers are sent to [http://localhost:3000/authority](http://localhost:3000/authority), where they can review their department's assigned queue and record status changes with a reason. A citizen can sign in to the citizen dashboard and expand a complaint's status history.

Department assignment emails are disabled by default. To test with a local SMTP capture service such as Mailpit, add these settings to the ignored `backend/application-local.properties` file:

```properties
civic.notifications.email.enabled=true
civic.notifications.email.from=civic-transparency@localhost
civic.notifications.email.department-recipients.ROADS=test-inbox@localhost

spring.mail.host=localhost
spring.mail.port=1025
```

Replace `ROADS` with the department's configured code and set the recipient to the test inbox address accepted by your SMTP capture service. The email contains only the complaint reference, department name, and portal link—not the complaint description or citizen identity. Real SMTP credentials should be stored only in the ignored local config or a secret manager, never committed.

## Stop the services

Press `Ctrl+C` in each terminal running the backend and frontend. PostgreSQL can remain running independently.
