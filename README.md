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

Before starting Tomcat, configure the database connection for the backend:

```bash
export STUDYFINDER_DB_URL='jdbc:mysql://127.0.0.1:3306/studyfinder_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export STUDYFINDER_DB_USER='root'
export STUDYFINDER_DB_PASSWORD='your_mysql_password'
```

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
