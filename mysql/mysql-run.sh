#!/bin/sh

# Source file download
git clone https://github.com/wikibook/realmysql80.git
unzip ./realmysql80/employees.zip -d ./realmysql80

# Copy source SQL file
mkdir entrypoint
touch ./entrypoint/init.sql
echo 'CREATE DATABASE employees DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE employees;' > ./entrypoint/init.sql
cat ./realmysql80/employees.sql >> ./entrypoint/init.sql

# Run SQL
docker run --name realmysql -v $PWD/entrypoint:/docker-entrypoint-initdb.d -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -p 3306:3306 -d mysql:8.0.39

rm -rf realmysql80
