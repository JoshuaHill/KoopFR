# KoopFR
Projektseminar Kooperationstechnologie - Gesichtserkennung mit OpenCV und JavaCV
Instructions

1. Prerequisites
    - Download and install OpenCV 3.0 from http://opencv.org
    	include native library (e.g., opencv/build/java/x64) in the java native library path
    - Download and install JavaCV from https://github.com/bytedeco/javacv
    
    - the database of known faces is below media/
    - the collection of example pictures for a person is in a subdirectory with the person's name,
    e.g. media/Christian Rathke/image-0.png, media/Christian Rathke/image-1.png, ...
    
2. Operation
	- run "java <args> de.hdm.faceCapture.FaceRecogApp" on the command line replacing <args> by
		a) -classpath FaceRecognizer.jar;FaceRecognizer_lib
		and
		b) -Djava.library.path=lib/x64 -DCaptureDevice=0
		
	- the capture device no. may need to be adapted (eg., 1, 2, 3,...)
	- a person's database of example pictures may be augmented by directly taking his picture from the camera
	or by importing and	saving it from an image which contains his face
	- each time a picture is added the face recognizer is retrained with all of the existing pictures