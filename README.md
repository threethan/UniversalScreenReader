# Universal Screen Reader
Provides a powerful interface to read text aloud from images or the clipboard.

![image](https://github.com/threethan/UniversalScreenReader/assets/12588584/62741e63-7055-4dbd-847a-203e85af242e)

### Key features:
- Automatically extract text from images using [Tesseract OCR](https://github.com/tesseract-ocr/tesseract)
- Read text from any application by taking a screenshot, then opening it from the clipboard
- Keep the reader window in front or use it as an overlay
- Optionally, automatically read newly copied text and images from the clipboard - even when the reader is minimized
- Clean & simple interface which matches system transparency and dark mode settings
- Supports Windows, macOS, and Linux (*Linux is missing transparency)

### This can be useful as:
- An accessibility feature for sighted people who have trouble reading,
- A tool for reviewing or revising text
- A tool for speed-reading

... or if you simply prefer listening to reading.

_Special thanks to Alex Allen for the idea, testing and feedback._

# Installation
Go to [releases](https://github.com/threethan/UniversalScreenReader/releases) and download the appropriate installer for your platform. "Universal Screen Reader" can then be opened as you would open any other application.
 

# Usage
Select a voice which matches the language of your text.

Open some text from the clipboard, or open an image from the clipboard or a file to automatically extract text.

Then, speak it aloud.

## Screenshotting Text
You can open images from your clipboard with a single button press (or, optionally, automatically).
This lets you conveniently read out text from any format by screenshotting part of your screen.

**On Windows,** use `Win+Ctrl+S` and then select a region. You can also screenshot a window, or the entire screen.

**On macOS,** use `Ctrl+Shift+Cmd+4` and then select a region. This keyboard shortcut may be changed in macOS's System Preferences, under `Keyboard -> Keyboard Shortcuts -> Screenshots`

**On Linux,** you may be able to use `Shift+Ctrl+PrintScreen`. If not, look into [gnome-screenshot](https://linux.die.net/man/1/gnome-screenshot)

## Downloading OCR Models
The OCR language is selected based on the current voice's locale.
The first time you open an image in a new language, an internet connection is required to download the
Tesseract OCR model for that language.

Alternatively, you can download models [here](https://github.com/tesseract-ocr/tessdata_best)
and place them in a folder named `TesseractData` in the path from which the jar is executed.

## More Info
For an in-depth explanation of all controls and shortcuts, check [FUNCTIONS.md](https://github.com/threethan/UniversalScreenReader/blob/main/FUNCTIONS.md)

For documentation on building or running from source, check [BUILD.md](https://github.com/threethan/UniversalScreenReader/blob/main/BUILD.md)
