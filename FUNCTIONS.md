# Controls
### Speak Text or Stop Speaking
**Shortcut: Ctrl+Space**

Use the first button on the bottom left of the window to speak text.
If some text is selected/highlighted, only that will be spoken. Otherwise, the full text will be spoken.
This button will be disabled if no text is currently open.

While text is being spoken, the sentence or line being read will be darkened.

While text is being spoken, the button will change to a stop icon and will instead stop speech. Speech cannot be paused.

### Open an Image from a File
**Shortcut: Ctrl+O**

Use the second button on the bottom-left of the window to open a file picker, then open an image file. png, gif, and jpg are supported.

Text from the image will be extracted automatically.

### Open an Image or Text from the Clipboard
**Shortcut: Ctrl+Shift+V**

Use the third button on the bottom-left of the window to open text or image content from the system clipboard.

The button will be disabled if the clipboard was just opened and hasn't yet been changed.
The icon on this button will change to reflect whether the clipboard contains text or an image.

If an image is opened, text from the image will be extracted automatically.

### Select a Voice for Speech
Use the first dropdown at the bottom of the window to select the voice to use for speech.

Voices are provided by the system. On Windows, additional voices may be installed using
*Settings -> Time and Language -> Add Language*

It is recommended to select a voice appropriate to the language and locale of text before opening any image,
as this will inform the model used by Tesseract OCR.

On systems with an extremely large number of voices (Linux), some voices will be hidden until you select their language.

_If the window has been set to a floating overlay, this option will be moved to the settings menu._

### Select a Speed for Speech
Use the second dropdown at the bottom of the window to select the speed to use for speech.

Speeds are given descriptive names, as the exact speed of playback may vary based on system.
Speed may be selected independently of voice.

_If the window has been set to a floating overlay, this option will be moved the settings menu._

### Open Settings Menu
Use the button on the bottom-right of the window to open the settings menu.

# Settings
### Allow Text Editing
If enabled, opened text may be edited manually.
All edits will be lost when text or an image is opened, or if the window is closed.

### Text Recognition
Specifies what process should be used to extract text from opened images.
- Use "Tesseract OCR (Local)" to locally process text via Tesseract.
- Use "Connect to a Server" to attempt to use a locally-hosted server.

### Window
#### New Window
Creates a new window using the same settings as this one

_This option will not be visible if the window is a floating overlay_

#### Switch to Floating Overlay
**Shortcut: Ctrl+Alt+O** or **Ctrl+Shift+O**

Switches the window to a floating overlay.
If a floating overlay already exists, the existing overlay will be returned to a normal window.
- The floating overlay is a small, translucent window which contains only the necessary controls.
- It will appear above *all* other windows, including the Windows TaskBar.
- It can be moved by dragging the small separator lines between buttons.
- Opened text is not visible in the floating overlay, but the currently spoken line will appear in a tooltip.

_If the window is a floating overlay, this option will be replaced with an option to return to a normal window,
and an additional option to close the floating overlay will be present_

#### Keep on Top of Other Applications
If enabled, the window will be set to always-on-top, and will remain above other application windows when losing focus.

_This option will not be visible if the window is a floating overlay_

#### Show Blurred Image Behind Text
If enabled, a blurred version of the currently-open image will be displayed in the background of the window,
behind any extracted text. If text is opened directly from the clipboard, a placeholder image will instead be used.

_This option will not be visible if the window is a floating overlay_

### Automatically
#### Open Image on Copy
If enabled: Whenever you copy a new image to the clipboard, it will be opened in this app;
equivalent to manually pressing the open from clipboard button.

The clipboard will be monitored until all windows are closed.

_WARNING: When using server-based image processing, every image you copy will be sent to the server_

#### Open Text on Copy
If enabled: Whenever you copy a fragment of text to the clipboard, it will be opened in this app;
equivalent to manually pressing the open from clipboard button.

The clipboard will be monitored until all windows are closed.

#### Speak on Open
If enabled: Whenever text is opened from any source, automatically play it back;
equivalent to pressing the speak text button
