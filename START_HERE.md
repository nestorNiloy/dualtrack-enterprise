# Start Here — DualTrack Enterprise (beginner-friendly)

This guide assumes Windows 10/11 and that you have never created a GitHub repository from a local folder before.

## 0. Protect the original project

Do not edit, rename, delete, or push this code into your existing `dual-study-application-tracker` repository yet. This is a separate project folder and a separate repository. Your current CV links remain unchanged.

## 1. Install the tools

Install these tools first:

1. **Git for Windows:** https://git-scm.com/download/win
2. **Docker Desktop:** https://www.docker.com/products/docker-desktop/ — install it and start Docker Desktop. Wait until it says Docker is running.
3. **A code editor:** Visual Studio Code is fine: https://code.visualstudio.com/
4. **GitHub account:** https://github.com/ (use your existing account).

You do not need to install PostgreSQL separately; Docker Compose starts it for this project.

## 2. Extract the project ZIP

1. Download `dualtrack-enterprise-starter.zip` from this chat.
2. In Windows File Explorer, open your Downloads folder.
3. Right-click the ZIP and choose **Extract All...**.
4. Extract it somewhere easy to find, for example `C:\Users\YOUR_WINDOWS_USER\Projects\`.
5. You should now have a folder named `dualtrack-enterprise`.
6. Open that folder in VS Code: **File → Open Folder...**.

Check that the folder contains `README.md`, `START_HERE.md`, `.gitignore`, `.github`, and `backend`.

## 3. Run it on your computer first

1. Start Docker Desktop and wait until it is fully running.
2. In VS Code, open **Terminal → New Terminal**.
3. In the terminal, change directory into the backend folder:

```powershell
cd backend
```

4. Start the application and PostgreSQL together:

```powershell
docker compose up --build
```

The first run can take several minutes because Docker downloads Java, Maven and PostgreSQL images and builds the application. Keep this terminal open while the app is running.

5. When the logs show that Spring Boot has started, open this address in your browser:

`http://localhost:8080`

6. Create an account in the sign-up panel. Use an email address you can identify and a password of at least 10 characters. This is a local development account.
7. Sign in and test the dashboard.
8. To stop the application, return to the terminal and press **Ctrl + C**. Then run:

```powershell
docker compose down
```

Your local database data remains in a Docker volume. To intentionally erase that local test data and start with an empty database, run `docker compose down -v`. Do not run that command if you want to keep your test account and records.

### If Docker says the port is already in use

Another application may already be using port 8080 or 5432. Stop the conflicting application and retry. Do not change ports randomly; ask for help with the exact error message.

### If the app does not start

Copy the first error and the last 40–60 lines of the terminal output. Do not guess at fixes or change database configuration yet.

## 4. Create a NEW GitHub repository

Do this only after you have extracted the new project. You are creating a separate repository; do not select or overwrite your old repository.

1. Visit https://github.com/new while signed in.
2. Repository name: `dualtrack-enterprise`.
3. Description: `A secure multi-user application management system built with Java, Spring Boot and PostgreSQL.`
4. Choose **Public** if you want recruiters to inspect the source code. Do not upload secrets or personal data.
5. Do **not** tick “Add a README”, “Add .gitignore”, or “Choose a license”. The downloaded project already contains a README and `.gitignore`.
6. Click **Create repository**.
7. Leave the new GitHub page open. It shows the repository URL, which looks like `https://github.com/YOUR_USERNAME/dualtrack-enterprise.git`.

## 5. Push your local project to the new GitHub repository

In VS Code, open a new terminal at the project root — the folder containing `README.md`, not inside `backend`.

Run these commands one at a time:

```powershell
git init
git add .
git status
git commit -m "Initial enterprise application tracker scaffold"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/dualtrack-enterprise.git
git push -u origin main
```

Replace `YOUR_USERNAME` with your actual GitHub username in the remote URL. Do not type the placeholder literally.

- `git init` creates new local Git history for this new project.
- `git add .` stages the project files.
- `git status` lets you review what will be committed. Confirm that no `.env`, password file, database dump or personal data is listed.
- `git commit` creates the first commit.
- `git remote add origin ...` connects this local folder to the NEW GitHub repository.
- `git push` uploads the code.

GitHub may open a browser sign-in or ask you to authenticate. Follow its prompts. Never paste a GitHub password or personal access token into this chat.

After the push, refresh the new GitHub repository page. You should see the project files and the `.github/workflows/ci.yml` workflow.

## 6. Check the automated build

1. On the new GitHub repository, click **Actions**.
2. Open the workflow named **CI**.
3. Wait for it to finish. A green check means Maven's `clean verify` passed in GitHub Actions. A red X means it failed; open the run and copy the error log.

The workflow runs on pushes to `main` and `develop`, and pull requests targeting `main`.

## 7. What not to do yet

- Do not change the old repository's Git remote or push this project to it.
- Do not point your existing CV or portfolio at this new project yet.
- Do not deploy this to a public production URL until authentication, CSRF, database configuration and frontend behavior have been verified.
- Do not reuse the old SQLite database as the new PostgreSQL production database without a separate data migration and backup plan.
- Do not commit credentials, `.env` files, production database URLs, or real application data.

## 8. What we need to finish next

The backend foundation and authentication UI are in place, but before this becomes a portfolio-ready live replacement we still need to:

1. Run and fix `mvn clean verify` in CI.
2. Complete frontend pagination and use the server-side statistics endpoint.
3. Add edit/update controls and a status-history display to the dashboard.
4. Add stronger integration tests for registration, login, CSRF, and Alice/Bob data isolation.
5. Verify the app end-to-end with two separate accounts.
6. Deploy the app and PostgreSQL database, then verify the public URL.
7. Only after that, merge the work into the original repository with a clean history and update the CV links.

**First milestone:** get the app running locally with Docker Compose, push it to the new GitHub repository, and confirm that the GitHub Actions CI run is green.
