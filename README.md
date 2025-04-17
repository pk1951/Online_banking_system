# TRUE Bank Online Banking System

A comprehensive online banking system built with Spring Boot, offering a complete set of banking features for customers and bank staff.

## Features

### User Features
- **Account Management**: Create and manage savings and current accounts
- **Transactions**: Deposit, withdraw, and transfer funds between accounts
- **Loan Management**: Apply for loans, track loan status, and make repayments
- **Transaction History**: View detailed transaction history with reference numbers
- **User Profile**: Update personal information and contact details

### Admin/Banker Features
- **Customer Management**: View and manage customer accounts
- **Loan Approval**: Review and process loan applications
- **Branch Management**: Manage branch information and staff
- **System Monitoring**: Track system activity and user actions

## Technology Stack

- **Backend**: Java, Spring Boot, Spring MVC, Spring Security, JPA/Hibernate
- **Frontend**: Thymeleaf, Bootstrap, HTML/CSS, JavaScript
- **Database**: MySQL (configured for easy switch to other databases)
- **Security**: JWT authentication, role-based access control
- **Development**: Maven, Git

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/online-banking-system.git
   cd online-banking-system
   ```

2. Configure database connection in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/truebank
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

3. Build and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. Access the application at [http://localhost:8080](http://localhost:8080)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Special thanks to all contributors to this project
- Inspired by modern banking systems and best practices in financial software development
