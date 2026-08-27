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
| JavaFX | Graphical User Interface |
| MySQL | Database management |
| JDBC | Java-MySQL connectivity |
| Maven | Build and dependency management |
| SSL/TLS | Secure client-server communication |
| TCP | Reliable message communication |
| UDP/NIO | Online/offline status heartbeat |
| BCrypt | Password security |

---

## 🏗️ System Architecture

```text
                 ┌────────────────────┐
                 │    JavaChat GUI    │
                 │      JavaFX        │
                 └─────────┬──────────┘
                           │
                           ▼
                 ┌────────────────────┐
                 │     ChatClient     │
                 └─────────┬──────────┘
                           │
                      SSL/TLS + TCP
                           │
                           ▼
                 ┌────────────────────┐
                 │     ChatServer     │
                 └─────────┬──────────┘
                           │
                    ┌──────┴──────┐
                    ▼             ▼
             ┌─────────────┐ ┌─────────────┐
             │ClientHandler│ │ UDP / NIO   │
             │             │ │  Heartbeat  │
             └──────┬──────┘ └─────────────┘
                    │
                    ▼
             ┌─────────────┐
             │   MySQL DB  │
             └─────────────┘

🔄 Communication Flow :
1. Login
User
  ↓
Login Window
  ↓
UserDAO
  ↓
MySQL Database
  ↓
Login Success
  ↓
Chat Window

2. Message Communication
Sender
  ↓
ChatClient
  ↓
SSL/TLS + TCP
  ↓
ChatServer
  ↓
ClientHandler
  ↓
Receiver

3. Online Status
ChatClient
  ↓
UDP/NIO Heartbeat
  ↓
ChatServer
  ↓
Heartbeat Timestamp
  ↓
Online / Offline Status
  ↓
Last Seen

📂 Project Structure
JavaChatApp/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── javachat/
│       │           │
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

The project demonstrates the practical implementation of:

Client-server architecture
Secure networking
Real-time communication
Database connectivity
Authentication
Online status monitoring
File sharing
User management
