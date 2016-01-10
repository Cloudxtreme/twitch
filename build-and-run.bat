@echo off
javac -g -cp json.jar -source 1.7 -target 1.7 Main.java
jar cfm tsc.jar MANIFEST.MF *.class siren.wav
java -jar tsc.jar -T 1 -U alicebiscuit
pause

