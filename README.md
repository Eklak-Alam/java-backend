🚀 Getting Started
Prerequisites
Java 17+ (Download)

MySQL 8.0+ (Download)

Maven 3.8+ (Download)

🔧 Backend Setup (Spring Boot)
1. Clone the Repository
bash
git clone https://github.com/your-repo/lic-certificate-generator.git
cd lic-certificate-generator/backend
2. Database Configuration
sql
CREATE DATABASE licdb;
CREATE USER 'lic_user'@'localhost' IDENTIFIED BY 'SecurePass123!';
GRANT ALL PRIVILEGES ON licdb.* TO 'lic_user'@'localhost';
FLUSH PRIVILEGES;
3. Configure Application
Edit src/main/resources/application.properties:

properties
spring.datasource.url=jdbc:mysql://localhost:3306/licdb
spring.datasource.username=lic_user
spring.datasource.password=SecurePass123!
4. Build and Run
bash
mvn clean install
mvn spring-boot:run
💻 Frontend Setup (Next.js)
1. Navigate to Frontend
bash
cd ../frontend
2. Install Dependencies
bash
npm install
3. Configure API Base URL
Create .env.local:

env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
4. Start Development Server
bash
npm run dev
🌐 Access the Application
Frontend: http://localhost:3000

Backend API: http://localhost:8080/api

🔐 First-Time Login
Use these default credentials:

Admin Panel:

Username: EklakAdmin

Password: eklak123

User Account:

Username: Alam

Password: alam

Change passwords immediately after first login!

🛠️ Troubleshooting
Issue	Solution
Port 8080 in use	sudo lsof -i :8080 then kill -9 <PID>
MySQL connection failed	Verify service is running: sudo systemctl status mysql
Certificate generation fails	Check storage directory permissions
Frontend not connecting	Verify CORS settings in WebSecurityConfig.java
