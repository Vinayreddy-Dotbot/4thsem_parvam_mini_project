@echo off
setlocal
cd /d "%~dp0"
echo Opening local preview at http://127.0.0.1:4177
start "" http://127.0.0.1:4177
python -m http.server 4177 --bind 127.0.0.1
endlocal
