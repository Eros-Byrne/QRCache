# Features

What QRCache does, screen by screen, as of v1.1.0 (the final release).

## Screens

The app has three main places, switched with a bottom tab bar:

1. **Scan** (opens by default)
2. **History**
3. **Settings**

Plus a **Scan detail** sheet that opens over either Scan or History.

### 1. Scan

- Camera preview fills the screen the moment the app opens.
- A **QR code / Barcode** switch at the top picks what to read. Barcode mode reads product and ticket barcodes (EAN, UPC, Code 128, Code 39, Code 93, Codabar, ITF) and uses a wide, short viewfinder.
- A soft rounded viewfinder in the center (square in QR mode). No need to line up perfectly; any code in frame is read.
- On a successful read: a light haptic tap, the viewfinder briefly turns green, and the **Scan detail** sheet slides up.
- Flashlight toggle (top right).
- **Scan from image** button: pick a photo or screenshot from the gallery and read the code in it, in whichever mode is selected.
- First launch asks for camera permission with a short explanation. If denied, show a friendly screen with a button to open settings, plus "Scan from image" which works without the camera.

### 2. Scan detail sheet

Shows one scan:

- **Title**, editable inline. Defaults to the page title for links (fetched only if the user allows it in Settings, otherwise the domain), the network name for Wi-Fi, or the first line of text.
- **Type** icon and label: Link, Text, Wi-Fi, Contact, Email, Phone, SMS, Location, Calendar event.
- **Content**, with the full value shown.
- **Date and time** scanned.
- **Primary action** button, based on type: Open link, Join Wi-Fi, Add contact, Call, Send email, Open in Maps, Add to calendar.
- Secondary actions: Copy, Share, Rename, Delete, Favorite.
- Scan count, when a code has been scanned more than once.

### 3. History

- List of all scans, newest first, grouped by **Today**, **Yesterday**, **This week**, **Earlier**.
- Each row: type icon, title, short content preview (domain for links), time.
- **All / QR codes / Barcodes** switch at the top. Scans from before v1.1 count as QR codes.
- **Search field** under it. Searches title, content and domain as you type.
- **Filter chips** under search: All, Favorites, Links, Wi-Fi, Text, Other.
- **Swipe left** on a row to delete (with Undo snackbar). **Swipe right** to favorite.
- Empty state: an icon, a short explanation and a "Scan your first code" button.
- **Duplicate handling:** scanning the same code again moves the existing entry to the top and updates its "last scanned" time instead of creating a copy. The scan count is shown in the detail sheet.

### 4. Settings

- **Theme:** System, Light, Dark.
- **Haptics** on/off.
- **Open links automatically** after scan (off by default).
- **Fetch page titles for links** (off by default; when on, the app makes one request to the link to read its title).
- **Browser:** in-app (Custom Tabs) or default browser.
- **Export history** (JSON or CSV) and **Import**.
- **Clear all history** (with confirmation).
- About: version and privacy policy.

## Ideas not built

QRCache is finished, so these won't be added. They're listed in [ROADMAP.md](ROADMAP.md#ideas-not-built) for anyone who forks the project: a home screen widget, a Quick Settings tile, notes on scans, folders or tags, opt-in scan location, creating your own QR codes, Google Drive backup, and an iOS app.

## Not doing

- Accounts or a server. History lives on the device.
- Ads or tracking.
