Setup Firebase google-services.json file in app directory.
Allow Firebase Auth for Rmail/ Password
Allow Firebase Realtime DB and set rules as 

{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
