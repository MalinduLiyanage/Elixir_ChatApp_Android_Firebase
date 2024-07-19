<h2>Overview</h2>
Elixir is a Android Chatting application which is based on Java, with Firebase Realtime database, Firebase Auth and Firebase Storage for its functionalities. The concept of the application is very simple. First, you have to be registered on the application by using your Email address. Then the application will force you to setup your account by updating the Name, Area, Status and Profile Picture. Each user is visible to each other, but if they need to establish a chat between two users, they first needs to be friends.

<h2>Features</h2>
<ul>
  <li>Use of Firebase Realtime Database, Firebase Auth and Firebase Storage</li>
  <li>User session Management</li>
  <li>Works realtime</li>
  <li>Show user's Active Status</li>
</ul>

<h2>Screenshots</h2>
<img src="https://github.com/user-attachments/assets/0ba59e0c-f35e-4b56-aa44-76c8612bcaf5" width="250" height="480">
<img src="https://github.com/user-attachments/assets/72b5af26-ddeb-4f11-8a29-766da8df09f5" width="250" height="480">
<img src="https://github.com/user-attachments/assets/c88337fc-0888-4657-88f2-44ec703ebe67" width="250" height="480">
<img src="https://github.com/user-attachments/assets/1d9f90db-84d3-4876-99c2-2cabe8098e0f" width="250" height="480">
<img src="https://github.com/user-attachments/assets/6bf2e4b5-abbf-4196-8abc-1d5d497fae11" width="250" height="480">
<img src="https://github.com/user-attachments/assets/0aeac260-2e52-431f-b1e9-0235b321cea7" width="250" height="480">
<img src="https://github.com/user-attachments/assets/9d5c98af-d633-4c9f-8ff4-e8e60b86bd7e" width="250" height="480">

<h2>How to Config</h2>
1. Setup Firebase google-services.json file in app directory.<br><tab>(Or you can do it by opening the repo in Android Studio)</tab></br>
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


