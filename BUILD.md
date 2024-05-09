# Building
## Run from source
The client can be run using `mvn javafx:run`

If you are on a Windows system with an intel gpu, uncomment `-Dprism.forceUploadingPainter=true` in the boot arguments to fix visual issues.

Tesseract binaries are included for Windows only.
[For Mac and Linux, install Tesseract manually,](https://tesseract-ocr.github.io/tessdoc/Installation.html)
or OCR will not work and the app may be unstable.

## Package from source
The client can be packaged using `mvn package -Dmaven.skip.test=true`. Packaging is supported for Windows, macOS, and Debian; but you must run this command from the target platform.

Remove `-Dmaven.skip.test=true` to run all unit tests. Unit tests will take a bit, and will output expected errors even on success.

To build, you _must_ have [JPackage](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html) on the system path.
JPackage requires [WiX](https://github.com/wixtoolset/wix3/releases) on Windows and Xcode tools on macOS.

Tesseract binaries are included for Windows only.
[For Mac and Linux, install Tesseract manually before building.](https://tesseract-ocr.github.io/tessdoc/Installation.html)

This process will also generate a usable jar in the `target/finalJar/` folder.

## JVM Arguments
You will not get transparency effects on windows (and will get some errors in the terminal) unless you run with
`java -jar /path/to/ocrapp.jar --add-opens javafx.graphics/javafx.stage=ALL-UNNAMED --add-opens javafx.graphics/com.sun.javafx.tk.quantum=ALL-UNNAMED`

If you are running Windows on an Intel GPU, add `-Dprism.forceUploadingPainter=true` to correct an issue where the UI appears overly translucent. _Do not use this argument on macOS._

On macOS, add `-Dapple.awt.application.appearance=system` to fix an issue where the title bar incorrectly displays as white, even on dark mode. 

Platform-appropriate JVM options are included in all the JPackage builds, so this only matters when running directly from a jar.