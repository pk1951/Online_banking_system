# GitHub Setup Instructions

Follow these steps to add your TRUE Bank Online Banking System to GitHub:

## Prerequisites
- Git installed on your computer
- GitHub account created (username: pk1951)
- GitHub CLI (optional, for easier authentication)

## Steps to Push to GitHub

1. **Initialize Git Repository** (if not already done):
   ```bash
   git init
   ```

2. **Add Files to Git**:
   ```bash
   git add .
   ```
   Note: The `.gitignore` file has been configured to exclude your actual application.properties file with sensitive credentials.

3. **Commit Changes**:
   ```bash
   git commit -m "Initial commit of TRUE Bank Online Banking System"
   ```

4. **Create a New Repository on GitHub**:
   - Go to https://github.com/new
   - Repository name: online-banking-system
   - Description: A comprehensive online banking system built with Spring Boot and Supabase
   - Choose Public or Private visibility
   - Do not initialize with README, .gitignore, or license (we already have these)
   - Click "Create repository"

5. **Add Remote Repository**:
   ```bash
   git remote add origin https://github.com/pk1951/online-banking-system.git
   ```

6. **Push to GitHub**:
   ```bash
   git push -u origin master
   # or if you're using 'main' as your default branch
   git push -u origin main
   ```

## Important Security Notes

1. **Credentials Protection**:
   - The actual application.properties file with your Supabase credentials is excluded from Git
   - Only the template file with placeholders is committed
   - Double-check with `git status` before pushing to ensure sensitive files are not included

2. **For Collaborators**:
   - They should copy application.properties.template to application.properties
   - Then add their own Supabase credentials following the README instructions

## Troubleshooting

If you encounter authentication issues when pushing to GitHub:
- Use GitHub CLI: `gh auth login`
- Or set up SSH keys for GitHub
- Or use a personal access token with the HTTPS URL
