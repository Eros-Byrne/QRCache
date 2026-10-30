# Features

What QRCache does, screen by screen. v1 is everything marked **v1**; the rest is planned.

## Screens

The app has three main places, switched with a bottom tab bar:

1. **Scan** (opens by default)
2. **History**
3. **Settings**

Plus a **Scan detail** sheet that opens over either Scan or History.

### 1. Scan (v1)

- Camera preview fills the screen the moment the app opens. Target: ready to scan in under 1 second on a mid-range phone.
- A soft rounded viewfinder in the center. No need to line up perfectly; any code in frame is read.
- On a successful read: a light haptic tap, the viewfinder briefly turns green, and the **Scan detail** sheet slides up.
- Flashlight toggle (top right).
- **Scan from image** button: pick a photo or screenshot from the gallery and read the code in it.
- First launch asks for camera permission with a short explanation. If denied, show a friendly screen with a button to open settings, plus "Scan from image" which works without the camera.

### 2. Scan detail sheet (v1)

Shows one scan:

- **Title**, editable inline. Defaults to the page title for links (fetched only if the user allows it in Settings, otherwise the domain), the network name for Wi-Fi, or the first line of text.
- **Type** icon and label: Link, Text, Wi-Fi, Contact, Email, Phone, SMS, Location, Calendar event.
- **Content**, with the full value shown.
- **Date and time** scanned.
- **Primary action** button, based on type: Open link, Join Wi-Fi, Add contact, Call, Send email, Open in Maps, Add to calendar.
- Secondary actions: Copy, Share, Rename, Delete, Favorite.

### 3. History (v1)

- List of all scans, newest first, grouped by **Today**, **Yesterday**, **This week**, **Earlier**.
- Each row: type icon, title, short content preview (domain for links), time.
- **Search bar** at the top, pulled down to reveal (like iOS). Searches title, content and domain as you type.
- **Filter chips** under search: All, Favorites, Links, Wi-Fi, Text, Other.
- **Swipe left** on a row to delete (with Undo snackbar). **Swipe right** to favorite.
- **Long press** to rename or select multiple for delete.
- Empty state: a friendly illustration and "Scan your first code" button.
- **Duplicate handling:** scanning the same code again moves the existing entry to the top and updates its "last scanned" time instead of creating a copy. The scan count is shown in detail.

### 4. Settings (v1)

- **Theme:** System, Light, Dark.
- **Haptics** on/off.
- **Open links automatically** after scan (off by default).
- **Fetch page titles for links** (off by default; when on, the app makes one request to the link to read its title).
- **Browser:** in-app (Custom Tabs) or default browser.
- **Export history** (JSON or CSV) and **Import**.
- **Clear all history** (with confirmation).
- About, version, privacy policy, open-source licenses.

## Planned later

| Feature | Milestone |
|---|---|
| Home screen widget (one-tap scan, recent items) | v1.1 |
| Quick Settings tile to open the scanner | v1.1 |
| Barcodes (EAN, UPC, Code 128) in addition to QR | v1.1 |
| Notes on a scan | v1.1 |
| Folders or tags ("Hotel trip", "Restaurants") | v1.2 |
| Location where a code was scanned (opt-in) | v1.2 |
| Create and share your own QR codes | v1.2 |
| Optional backup to the user's own Google Drive | v2 |
| iOS app | v2 |

## Not doing

- Accounts or a server. History lives on the device.
- Ads or tracking.
