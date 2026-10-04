# TCP Group Chat Application
**Mini Project 1 – Network Programming (Java Sockets + JavaFX)**

A real-time group chat system built with Java TCP Sockets and JavaFX, following a strict **MVC / Separation-of-Concerns** architecture.

---

## 🏗️ Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                         SERVER                                   │
│  ServerApp ──► ServerController ──► ServerModel (Sockets/IO)    │
│                     │                                            │
│               ServerView (JavaFX)                                │
└──────────────────────────────────────────────────────────────────┘
            ▲              ▲              ▲
          TCP           TCP           TCP
            │              │              │
┌───────────┘  ┌───────────┘  ┌──────────┘
│  CLIENT A    │  CLIENT B    │  CLIENT C
│  ClientApp   │  ClientApp   │  ClientApp
│  Controller  │  Controller  │  Controller
│  Model       │  Model       │  Model
│  View (FX)   │  View (FX)   │  View (FX)
└──────────────┘──────────────┘──────────────┘
```

The **Model** (socket I/O) is **completely independent** of the **View** (JavaFX). Changes to the UI never require changes to the networking code.

---

## ✨ Features

### Client
- **Authentication / Identity**: Enter a username on the login screen before chatting.
- **Read-Only Mode**: Connect without a username → observe messages but cannot send.
- **Real-Time Messaging**: Type + press Enter or click **SEND**.
- **`allUsers` command**: Lists all currently connected users.
- **`bye` / `end` command**: Graceful disconnection.
- **Online/Offline status indicator** (colored dot + label).

### Server
- **Multi-client support** via thread-per-connection model.
- **Message distribution**: Formats messages with username + timestamp, broadcasts to all.
- **Live user list**: ListView of connected users with random pastel color per user.
- **Activity log**: Shows "Server Started", "Welcome [User]", disconnections, etc.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Networking | Java Sockets (TCP) |
| GUI | JavaFX 21 + GridPane / CSS |
| Build | Maven |
| IDE | IntelliJ IDEA |

---

## 🚀 Running the Application

### Prerequisites
- Java 17+
- Maven 3.8+

### Build

```bash
# Build Server
cd TCPServer
mvn clean package

# Build Client
cd ../TCPClient
mvn clean package
```

### Run

```bash
# Start Server (reads port from server.properties)
java -jar TCPServer/target/TCPServer-1.0-SNAPSHOT.jar

# Start Client (optional: override host/port via args)
java -jar TCPClient/target/TCPClient-1.0-SNAPSHOT.jar localhost 3000
```

### Configuration

Edit `TCPServer/src/main/resources/server.properties`:
```properties
server.port=3000
server.host=localhost
```

Edit `TCPClient/src/main/resources/client.properties`:
```properties
server.host=localhost
server.port=3000
```

No recompilation needed — settings are loaded at runtime.

---

## 📁 Project Structure

```
chat-app/
├── TCPServer/
│   ├── pom.xml
│   └── src/main/java/com/chat/server/
│       ├── ServerApp.java            ← JavaFX entry point
│       ├── model/ServerModel.java    ← Socket logic, broadcasting
│       ├── view/ServerView.java      ← JavaFX UI
│       └── controller/ServerController.java
│
├── TCPClient/
│   ├── pom.xml
│   └── src/main/java/com/chat/client/
│       ├── ClientApp.java            ← JavaFX entry point
│       ├── model/ClientModel.java    ← Socket I/O, message parsing
│       ├── view/ClientView.java      ← Login + Chat screens
│       └── controller/ClientController.java
│
└── README.md
```

---

## 🔌 Server-Client Protocol

| Message (Server → Client) | Meaning |
|---|---|
| `SERVER:WELCOME:<username>` | Successful authentication |
| `SERVER:READ_ONLY` | Connected without username |
| `SERVER:JOIN:<username>` | Another user joined |
| `SERVER:LEAVE:<username>` | Another user left |
| `SERVER:USERS:<list>` | Response to `allUsers` command |
| `[HH:mm:ss] user: text` | Regular chat message |

| Command (Client → Server) | Meaning |
|---|---|
| `allUsers` | Request list of active users |
| `bye` / `end` | Graceful disconnection |
| *(any other text)* | Chat message |

---

## 📐 UML Diagrams

See `/uml/` folder for:
- **Class Diagram** – full MVC structure
- **Deployment Diagram** – network topology

---

## 👤 Author
ouzair bouaouida
