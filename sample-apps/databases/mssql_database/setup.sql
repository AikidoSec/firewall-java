-- Check if the database exists
IF NOT EXISTS (
    SELECT name 
    FROM sys.databases 
    WHERE name = 'db'
)
BEGIN
    -- Create the database if it does not exist
    CREATE DATABASE db;
END
GO

-- Use the database
USE db;
GO

-- Drop the table if it exists
IF OBJECT_ID('dbo.pets', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.pets;
END
GO

-- Create the table if it does not exist
CREATE TABLE dbo.pets (
    pet_id INT IDENTITY(1,1) PRIMARY KEY,          -- Use SERIAL for auto-increment
    pet_name VARCHAR(250) NOT NULL,     -- Use VARCHAR for string types
    owner VARCHAR(250) NOT NULL          -- Use VARCHAR for string types
);
GO

