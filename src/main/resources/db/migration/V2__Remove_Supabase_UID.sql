-- Remove supabase_uid column from users table
ALTER TABLE users DROP COLUMN IF EXISTS supabase_uid;
