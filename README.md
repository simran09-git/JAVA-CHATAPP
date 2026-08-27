# 💬 JavaChat

A secure real-time desktop chat application developed using Java.  
JavaChat provides private messaging, file sharing, online/offline status, last seen, user blocking, chat history, secure authentication, and database storage.

---

## 🚀 Features

- 🔐 User Registration & Login
- 🔒 Secure Password Hashing using BCrypt
- 💬 Real-time Private Messaging
- 📤 Two-way Message Communication
- 📁 File Sharing
- 🟢 Online / Offline Status
- 🕐 Last Seen
- 🚫 Block / Unblock Users
- 📜 Chat History
- 👤 User Profile
- 🔑 Change Password
- ⚙️ Settings
- 🌙 Dark Mode
- 🔔 Message Notifications
- 🔊 Message Sound
- ↵ Enter-to-Send
- 🔐 SSL/TLS Secure Communication
- 💾 MySQL Database Storage
- ❤️ UDP/NIO Heartbeat for Online Status

---

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Main programming language |
| Swing | Desktop GUI |
| AWT | GUI layouts and desktop components |
| MySQL | Database management |
| JDBC | Java-MySQL connectivity |
| Maven | Build and dependency management |
| SSL/TLS | Secure communication |
| TCP | Reliable message communication |
| UDP/NIO | Online status heartbeat |
| BCrypt | Password security |

---

## 🏗️ System Architecture

```text
                ┌──────────────────┐
                │    JavaChat GUI  │
                │   Swing + AWT    │
                └────────┬─────────┘
                         │
                         ▼
                ┌──────────────────┐
                │    ChatClient    │
                └────────┬─────────┘
                         │
                    SSL/TLS + TCP
                         │
                         ▼
                ┌──────────────────┐
                │    ChatServer    │
                └────────┬─────────┘
                         │
                  ┌──────┴──────┐
                  ▼             ▼
          ┌─────────────┐ ┌─────────────┐
          │ClientHandler│ │ UDP / NIO   │
          │             │ │ Heartbeat   │
          └──────┬──────┘ └─────────────┘
                 │
                 ▼
          ┌─────────────┐
          │  MySQL DB   │
          └─────────────┘

Project Structure:

JavaChatApp/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── javachat/
│       │           ├── client/
│       │           │   └── ChatClient.java
│       │           │
│       │           ├── server/
│       │           │   ├── ChatServer.java
│       │           │   ├── ClientHandler.java
│       │           │   └── ClientManager.java
│       │           │
│       │           ├── database/
│       │           │   ├── DatabaseConnection.java
│       │           │   ├── UserDAO.java
│       │           │   └── MessageDAO.java
│       │           │
│       │           └── ui/
│       │               ├── LoginWindow.java
│       │               ├── RegisterWindow.java
│       │               ├── ChatWindow.java
│       │               ├── ProfileWindow.java
│       │               └── SettingsWindow.java
│       │
│       └── resources/
│           ├── javachat.p12
│           └── css/
│
├── pom.xml
└── README.md

🎯 Project Objective

The main objective of JavaChat is to develop a secure and reliable real-time desktop messaging application using Java networking, database connectivity, secure communication, and graphical user interface technologies.
