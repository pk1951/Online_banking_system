# TRUE Bank Online Banking System

A comprehensive online banking system built with Spring Boot, offering a complete set of banking features for customers and bank staff. This application uses Supabase as its database provider.

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
- **Database**: PostgreSQL via Supabase
- **Security**: JWT authentication, role-based access control
- **Development**: Maven, Git

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Supabase account (for database)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/pk1951/online-banking-system.git
   cd online-banking-system
   ```

2. Set up Supabase:
   - Create a Supabase account at [https://supabase.com](https://supabase.com)
   - Create a new project
   - Get your project URL and anon key from the API settings
   - Set up a PostgreSQL database in your Supabase project

3. Configure application properties:
   - Copy `src/main/resources/application.properties.template` to `src/main/resources/application.properties`
   - Update the following properties with your Supabase credentials:
     ```properties
     # Database Configuration (Supabase)
     spring.datasource.url=jdbc:postgresql://<SUPABASE_HOST>:<SUPABASE_PORT>/postgres
     spring.datasource.username=<SUPABASE_USERNAME>
     spring.datasource.password=<SUPABASE_PASSWORD>

     # Supabase Configuration
     supabase.url=<SUPABASE_PROJECT_URL>
     supabase.key=<SUPABASE_ANON_KEY>
     ```

4. Build and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. Access the application at [http://localhost:8080](http://localhost:8080)

## Setting Up Supabase Credentials

### How to Get Supabase Credentials

1. **Create a Supabase Account**:
   - Go to [https://supabase.com](https://supabase.com) and sign up for an account
   - Verify your email address

2. **Create a New Project**:
   - Click on "New Project" in the Supabase dashboard
   - Enter a name for your project
   - Set a secure database password
   - Choose a region close to your users
   - Click "Create new project"

3. **Get Your Project URL and Anon Key**:
   - In your project dashboard, go to Project Settings > API
   - Under "Project URL", copy your project URL (e.g., https://yourproject.supabase.co)
   - Under "Project API Keys", copy the "anon public" key

4. **Get Database Connection Information**:
   - In your project dashboard, go to Project Settings > Database
   - Find the "Connection String" section
   - Copy the host, port, database name, and username

5. **Update Your Application Properties**:
   - Replace the placeholders in your `application.properties` file with the actual values

### Security Best Practices

- Never commit your actual Supabase credentials to GitHub
- Use environment variables in production environments
- Regularly rotate your API keys for enhanced security
- Set up IP restrictions in Supabase for your database

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Special thanks to all contributors to this project
- Inspired by modern banking systems and best practices in financial software development
