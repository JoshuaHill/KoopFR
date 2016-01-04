@ECHO OFF
rem ---------------------------------------
rem - Wechsle Verzeichnis zum Verzeichnis der BAT-Datei
rem ---------------------------------------

cd %~dp0
java -classpath .;FaceRecognizer.jar;FaceRecognizer_lib -Djava.library.path=lib/x64 -DCaptureDevice=1 de.hdm.faceCapture.FaceRecogApp

echo --
echo Launching Face Recognition ...
echo --
