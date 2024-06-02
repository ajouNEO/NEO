echo off
:start

cd steam
steamcmd.exe +runscript ../update_script.txt
steamcmd.exe +runscript ../update_script.txt
cd ..

cd rustds
RustDedicated.exe -batchmode +server.hostname "My Server Name" +server.port 28015 +server.identity "my_server" +server.seed 1234567 -logFile "output.txt" -autoupdate
cd ../

goto start