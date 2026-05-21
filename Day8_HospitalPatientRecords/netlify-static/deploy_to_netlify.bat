@echo off
setlocal
cd /d "%~dp0"
echo ==========================================================
echo HOSPITAL PATIENT RECORDS - NETLIFY DEPLOY
echo ==========================================================
echo.
echo This deploys the static Netlify version of the app.
echo If you want to deploy to your own Netlify account, run:
echo npx --yes netlify-cli@latest login
echo.
npx --yes netlify-cli@latest deploy --prod --dir "." --message "Hospital Patient Records static deployment" --allow-anonymous --no-build
endlocal
