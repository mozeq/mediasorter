chdir /D E:\mediasorter
echo "%~1" "%~2" >> debug.log
Set _downloadPath=%~1

IF "%_downloadPath:~-1%" == "\" (Set _downloadPath=%_downloadPath%\)

"C:\Program Files\java\jdk1.7.0_45\jre\bin\java.exe" -jar "target/mediasorter-ng-jar-with-dependencies.jar" "%_downloadPath%" "D:\Media" %2 >> mediasort.log