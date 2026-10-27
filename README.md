# SJSU Study Finder organized source

React + Vite + Tailwind CSS project 

- Runtime: React 19 and React DOM 19
- Styling: Tailwind CSS v4 with the `@tailwindcss/vite` plugin
- Build tooling: Vite 8, TypeScript 5.7, and `@vitejs/plugin-react`
- Formatting: oxfmt

## Running the JSP/MySQL version

The study-spots page is served directly by `study-spots.jsp`. It loads data through
`StudySpotService`, which calls `StudySpotDAO`; the DAO is the only layer that
executes SQL. No study-spot servlet is required.


## Database Configuration

Each team member must configure their own local MySQL connection.
These commands must be run in the same terminal session used to start the backend or Tomcat:

### Option 1: Configure Tomcat in Eclipse

1. Open the **Servers** view in Eclipse.
2. Right-click the Tomcat server.
3. Select **Open**.
4. Click **Open launch configuration**.
5. Select the **Environment** tab.
6. Click **New** and add the following variables:

| Variable | Value |
|---|---|
| `STUDYFINDER_DB_URL` | `jdbc:mysql://127.0.0.1:3306/studyfinder_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `STUDYFINDER_DB_USER` | `root` |
| `STUDYFINDER_DB_PASSWORD` | Your local MySQL password |

Do not include quotation marks around the values.

7. Click **Apply**.
8. Restart Tomcat.

### Option 2: Configure Variables in the Terminal

Before starting the backend or Tomcat, run these commands in your backend folder:

```bash
export STUDYFINDER_DB_URL='jdbc:mysql://127.0.0.1:3306/studyfinder?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export STUDYFINDER_DB_USER='root'
export STUDYFINDER_DB_PASSWORD='your_mysql_password'
export STUDYFINDER_SMTP_HOST='smtp.gmail.com'
export STUDYFINDER_SMTP_PORT='587'
export STUDYFINDER_SMTP_USER='your-email@gmail.com'
export STUDYFINDER_SMTP_PASSWORD='your-gmail-app-password'
export STUDYFINDER_SMTP_FROM='your-email@gmail.com'
```

Then start the backend or Tomcat from the same terminal session.

The terminal variables are temporary and must be configured again when opening a new terminal.

### Important Notes

- Replace `your_mysql_password` with your own local MySQL password.
- Do not commit real passwords or database credentials to Git.
- The database name `studyfinder_db` must match the database name in `sql/schema.sql`.
- Each team member must configure their own local MySQL connection.


## Open this webpage
Deploy `backend-v0.1/target/SJSUStudyFinder.war` to Tomcat and open:
`http://localhost:8080/SJSUStudyFinder/study-spots.jsp`

The login, signup, guest, and logout actions still use servlets because those
actions receive POST requests and create or destroy the HTTP session. That is
separate from loading the study-spot data.
## Authentication email setup

Signup uses the JSP `login.jsp` page and sends a six-digit verification code before creating the user. Configure these environment variables in the Eclipse Tomcat server's **Arguments > Environment** tab:

```text
STUDYFINDER_SMTP_HOST=smtp.gmail.com
STUDYFINDER_SMTP_PORT=587
STUDYFINDER_SMTP_USER=your-email@gmail.com
STUDYFINDER_SMTP_PASSWORD=your-gmail-app-password
STUDYFINDER_SMTP_FROM=your-email@gmail.com
```

The SMTP account needs an app password; do not use the normal Gmail password. The database connection variables remain:

```text
STUDYFINDER_DB_URL=jdbc:mysql://127.0.0.1:3306/studyfinder?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
STUDYFINDER_DB_USER=root
STUDYFINDER_DB_PASSWORD=your_mysql_password
```

# StudyFinder Backend

## Requirements

- Java
- Maven
- MySQL
- MySQL Connector/J
- Eclipse or another Java IDE

## Database Setup

1. Start MySQL.

2a. From the project folder, run to create and delete tables:

```bash
mysql -u root -p < sql/schema.sql
```

2b. From the project folder, run to insert sample data:
```bash
mysql -u root -p < sql/sample_data.sql
```
2c. 
```bash
mysql -u root -p < backend-v0.1/sql/migrations/alter_reports_add_wifi.sql
```