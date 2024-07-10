Setup Firebase google-services.json file in app directory.
Allow Firebase Auth for Rmail/ Password<br>
Allow Firebase Realtime DB and set rules as 

{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}

Allow Firebase Storage and set rules as<br>

rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      // Allow read and write access if request is authenticated
      allow read, write: if request.auth != null;
    }
  }
}

