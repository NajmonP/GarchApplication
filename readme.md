# Garch Application

Web application for modeling and analyzing financial time series using GARCH models.  
The application allows users to upload time series data, configure models, and perform volatility calculations.

## Run the Application

The recommended way to run the application is via Docker.

The following command will run projects docker setup.
It pulls the PostgreSQL image, builds the application image, and starts all containers.

```
docker compose up --build
```
The application will be available at: http://localhost:8080
PostgreSQL will be running on: localhost:5432

## Project structure

- data/ - sample input data for testing and modeling
- scripts/ - database initalization scripts (executed on first DB startup)
- src/ - source code


### Configuration Notes

Use `.env.example` as a template for creating your local `.env` file. The `.env` file contains PostgreSQL initialization variables.

Changes in `.env` do NOT require rebuilding images. However, these variables are applied only when the PostgreSQL volume is initialized for the first time.

To apply changes in `.env`, remove the existing volume and recreate the containers:

```
docker compose down -v
docker compose up
```

NOTE: This will DELETE all stored data.



