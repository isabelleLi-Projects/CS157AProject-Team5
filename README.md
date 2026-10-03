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
