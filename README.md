1. Setup Firebase google-services.json file in app directory.<br><tab>(Or you can do it by opening the repo in Android Studio)
2. Allow Firebase Auth for Email/ Password<br>
3. Allow Firebase Realtime DB and set rules as 

<pre><code>
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
</code></pre>

4. Allow Firebase Storage and set rules as

<pre><code>
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      // Allow read and write access if request is authenticated
      allow read, write: if request.auth != null;
    }
  }
}
</code></pre>


