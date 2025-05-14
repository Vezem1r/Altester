# Altester

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![License](https://img.shields.io/badge/license-MIT-green)

</div>

## 🔍 Overview

Altester is a robust and scalable educational platform developed as part of a bachelor's thesis, built using a microservices architecture. Designed for educational institutions, it optimizes the entire test lifecycle — from creation and distribution to AI-powered evaluation.

<p align="center">
  <img src="https://github.com/user-attachments/assets/2be1f7f7-fd20-49a3-bf63-c3d7701a27b5" alt="Image" />
</p>

<div align="center">

[![Live Demo](https://img.shields.io/badge/Live_Demo-Preview-brightgreen)](https://.com)

</div>

### Key Features

- **AI-Powered Grading** - Automated assessment with multiple AI models
- **Real-time Communication** - Instant messaging between students and teachers
- **Advanced Test Management** - Comprehensive test creation and administration
- **Role-Based Access** - Tailored experiences for administrators, teachers, and students
- **Enterprise Integration** - LDAP support for institutional deployment

## 📚 Documentation

- [Setup and Installation Guide](https://github.com/Vezem1r/Altester/wiki/Setup-and-Installation-Guide)
- [Environment Configuration](https://github.com/Vezem1r/Altester/wiki/Environment-Setup)
- [API Documentation](https://github.com/Vezem1r/Altester/wiki/API-Documentation)
- [System Architecture](https://github.com/Vezem1r/Altester/wiki/Architecture-Overview)

## 🛠️ Technology Stack

<details open>
<summary><b>Backend Technologies</b></summary>
<br>

| Technology | Purpose |
|------------|---------|
| **Java 21** | Core language for all microservices |
| **Spring Boot 3.4.5** | Application framework with consistent version across services |
| **Spring Security** | Authentication and authorization framework |
| **Spring Data JPA** | ORM for data access and persistence |
| **Spring Data LDAP** | LDAP directory integration |
| **PostgreSQL** | Primary relational database |
| **JWT (jjwt 0.11.5)** | Secure token-based authentication |
| **WebSockets** | Real-time bidirectional communication |
| **Redis** | High-performance caching layer |
| **LangChain4j 1.0.0-beta3** | Advanced AI integration framework |
| **Thymeleaf** | Template engine for email verification |
| **Spring Mail** | SMTP integration for notifications |
| **Jackson 2.15.2** | JSON processing library |
| **Spring Validation** | Request validation framework |
| **SpringDoc 2.8.6** | API documentation generation |
| **Docker & Compose** | Containerization and orchestration |

</details>

<details open>
<summary><b>Frontend Technologies</b></summary>
<br>

| Technology | Purpose |
|------------|---------|
| **React** | Modern UI library |
| **WebSocket Client** | Real-time communication |

</details>

## 🧩 Microservices Architecture

<details open>
<summary><b>1. Authorization Service</b></summary>
<br>

**Core Responsibilities:**
- User registration and account management
- Dual authentication methods:
  - Local username/password authentication
  - LDAP integration for enterprise environments
- Secure password management (reset, change) for local users
- Email management with verification
- OTP-based verification for all critical actions
- JWT token generation and management
- SMTP integration for all verification workflows
</details>

<details open>
<summary><b>2. Chat Service</b></summary>
<br>

**Core Responsibilities:**
- Real-time messaging between platform users
- Role-restricted communication channels:
  - Students can only message their assigned teachers
  - Teachers can message students in their assigned groups
- Conversation history and management
- WebSocket security with custom interceptors
- User online/offline status tracking
- Message read status indicators
- "User is typing" live indicators
</details>

<details open>
<summary><b>3. Notification Service</b></summary>
<br>

**Core Responsibilities:**
- System-wide notifications delivery
- User-specific alerts and messages
- Real-time delivery via WebSockets
- Notification history and management
</details>

<details open>
<summary><b>4. AI Grading Service</b></summary>
<br>

**Core Responsibilities:**
- Automated assessment of free-text responses
- Support for multiple AI providers:
  - OpenAI models
  - Anthropic Claude
  - Google Gemini
  - DeepSeek
- Integration with test workflow
- Submission processing in contextual prompts
- Secure API key management for AI services
- Response parsing for extracting:
  - Grades
  - Detailed feedback
  - Error explanations for students
</details>

<details open>
<summary><b>5. Core Service</b></summary>
<br>

**Core Responsibilities:**
- Central business logic coordination
- Subject creation and management
- Semester validation and academic period handling
- Student duplication validation across groups
- Group creation and membership management
- Test creation and configuration:
  - Question management (5 distinct formats)
  - Test parameters and settings
  - Time limits and attempt policies
- Test attempt lifecycle management:
  - Starting tests
  - Reconnecting to in-progress tests
  - Saving partial results
  - Completing attempts
  - Sending grading requests to AI service
- Placeholder validation and injection prevention in prompts
- Role-based API key management
</details>

## 👥 User Roles and Features

<div align="center">
  <table>
    <tr>
      <th align="center">
        <img width="30" height="30" src="https://img.icons8.com/color/48/administrator-male.png" alt="Administrator"/>
        <br>Administrator
      </th>
      <th align="center">
        <img width="30" height="30" src="https://img.icons8.com/color/48/teacher.png" alt="Teacher"/>
        <br>Teacher
      </th>
      <th align="center">
        <img width="30" height="30" src="https://img.icons8.com/color/48/student-male.png" alt="Student"/>
        <br>Student
      </th>
    </tr>
    <tr>
      <td>
        <ul>
          <li>Complete user management</li>
          <li>Subject & group management</li>
          <li>Test lifecycle management</li>
          <li>Question configuration</li>
          <li>Performance analytics</li>
          <li>API key management</li>
          <li>System configuration</li>
        </ul>
      </td>
      <td>
        <ul>
          <li>Access to assigned groups</li>
          <li>Test creation & management</li>
          <li>Student submission grading</li>
          <li>AI grade review</li>
          <li>Student communication</li>
          <li>Limited API key management</li>
        </ul>
      </td>
      <td>
        <ul>
          <li>Test participation</li>
          <li>Attempt resumption</li>
          <li>Test history access</li>
          <li>Teacher communication</li>
          <li>Notification reception</li>
        </ul>
      </td>
    </tr>
  </table>
</div>

<details>
<summary><b>Administrator Detailed Features</b></summary>
<br>

- Comprehensive user management
  - Student and teacher accounts
  - LDAP integration (LDAP-imported accounts have immutable core properties)
- Subject and group management
  - Creation, editing, and archiving
  - Assignment of teachers to groups
- Semester configuration via environment variables
- Complete test lifecycle management
  - Creation, updates, deletion
  - Teacher assignment and permissions
- Question management across all 5 supported types:
  - Text only
  - Text + image
  - Image only
  - Multiple choice
  - Multiple choice + image
- Student performance analytics by test and group
- System-wide configuration and monitoring
- API key management
</details>

<details>
<summary><b>Teacher Detailed Features</b></summary>
<br>

- Access to assigned groups and students
- Test creation and management
  - Create new tests
  - Edit owned or delegated tests
  - Configure question parameters
- Student submission evaluation
  - Manual grading
  - Review of AI-graded submissions
- Real-time communication with students
- Notification reception for relevant events
- Limited API key management
</details>

<details>
<summary><b>Student Detailed Features</b></summary>
<br>

- Access to current semester groups and subjects
- Test participation
  - Take active tests
  - Resume attempts within configured time limits
  - Submit answers for evaluation
- View comprehensive test history across semesters
- Real-time communication with assigned teachers
- System and test notifications
</details>

## 🌐 Universal Features

<div align="center">
  <table>
    <tr>
      <td align="center">
        <img width="40" height="40" src="https://img.icons8.com/color/48/password-check.png" alt="account"/>
        <br><b>Account Management</b>
        <br><small>Self-service password & email updates</small>
      </td>
      <td align="center">
        <img width="40" height="40" src="https://img.icons8.com/color/48/bell.png" alt="notifications"/>
        <br><b>Real-time Notifications</b>
        <br><small>Instant updates and alerts</small>
      </td>
      <td align="center">
        <img width="40" height="40" src="https://img.icons8.com/color/48/database.png" alt="pagination"/>
        <br><b>Data Pagination</b>
        <br><small>Efficient data management</small>
      </td>
    </tr>
  </table>
</div>

## ⚙️ Environment Configuration

Application is configured via environment variables in `.env` file. See our [detailed configuration guide](https://github.com/Vezem1r/Altester/wiki/Environment-Setup) for all options.

```bash
# Quick start configuration example
cp .env.example .env
# Edit your .env file with appropriate values
nano .env
# Start the services
docker-compose up -d
```

## 🚀 Getting Started

```bash
# Clone the repository
git clone https://github.com/Vezem1r/Altester.git
cd Altester

# Configure environment
cp .env.example .env
# Edit variables as needed

# Build and run
docker-compose build
docker-compose up -d

# Access the application
open http://localhost:5173
```

## 🛠️ Next Steps

<div align="center">
  <p>These are the enhancements to further improve Altester's architecture and performance:</p>
</div>

<details open>
<summary><b>1. API Gateway Implementation</b></summary>
<br>

**Planned Features:**
- Centralized request routing for all microservices
- Rate limiting to prevent abuse and ensure fair usage
- Load balancing
</details>

<details open>
<summary><b>2. Advanced Cache Management</b></summary>
<br>

**Improvements:**
- Granular cache invalidation strategies
  - Update specific cache entries instead of full purge

</details>

<details open>
<summary><b>3. Future Enhancements</b></summary>
<br>

**Under Consideration:**
- Kubernetes orchestration for better scalability
- GraphQL API alongside REST
- Machine learning for predictive test difficulty adjustment
</details>

<div align="center">
  <table>
    <tr>
      <td align="center">
        <img width="40" height="40" src="https://img.icons8.com/color/48/api.png" alt="api"/>
        <br><b>API Gateway</b>
        <br><small>2025</small>
      </td>
      <td align="center">
        <img width="40" height="40" src="https://img.icons8.com/?size=100&id=pHS3eRpynIRQ&format=png&color=000000" alt="cache"/>
        <br><b>Cache Optimization</b>
        <br><small>2025</small>
      </td>
      <td align="center">
        <img width="40" height="40" src="https://img.icons8.com/color/48/rocket.png" alt="future"/>
        <br><b>Future Features</b>
        <br><small>Ongoing</small>
      </td>
    </tr>
  </table>
</div>