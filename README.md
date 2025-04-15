## TaskBoard REST Service

This is a Spring Boot application for a task board REST service. It allows users to manage lists of tasks with the following features:

- View all lists and tasks
- Create an empty list
- Add tasks to a list
- Update tasks
- Delete tasks
- Delete lists
- Move tasks between lists


### Technologies Used

- Java 21
- Spring Boot 3.2.5
- Spring Data JDBC (for persistence)
- PostgreSQL (relational database)
- Liquibase (for database schema management)
- Swagger (for API documentation)
- Gradle (build tool)
- Testcontainers (for Dockerized integration tests)
- Docker (for containerization)
- AWS (for deployment)


### Prerequisites
- Java 21
- Gradle 8.x
- Docker and Docker Compose
- PostgreSQL (local or via Docker)
- AWS CLI (for deployment)


### Setup and Running Locally

1. Clone the Repository

```
git clone <repository-url>
cd taskboard
```

2. Start PostgreSQL

Run PostgreSQL using Docker:

`docker run -d --name postgres -p 5432:5432 -e POSTGRES_DB=taskboard -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:16-alpine`

3. Build the Application

`./gradlew build`

4. Run the Application

`./gradlew bootRun`

The application will be available at http://localhost:8080.

5. Access Swagger UI

Swagger UI is available at http://localhost:8080/swagger-ui.html for testing the API endpoints interactively.

6. Run Tests

The integration tests use Testcontainers to spin up a PostgreSQL container automatically:

`./gradlew test`



### API Endpoints


1. GET /api/taskboard/lists - View all lists and tasks

2. POST /api/taskboard/lists - Create a new list (body: {"name": "List Name"})

3. POST /api/taskboard/lists/{listId}/tasks - Add a task to a list (body: {"name": "Task Name", "description": "Task Description"})

4. PUT /api/taskboard/tasks/{taskId} - Update a task (body: {"name": "Updated Name", "description": "Updated Description"})

5. DELETE /api/taskboard/tasks/{taskId} - Delete a task

6. DELETE /api/taskboard/lists/{listId} - Delete a list

7. PUT /api/taskboard/tasks/{taskId}/move - Move a task to another list (body: {"newListId": 2})


### Database Schema Management

The application uses Liquibase to manage the database schema. The schema is defined in src/main/resources/db/changelog/db.changelog-master.yaml. Liquibase runs automatically on application startup to apply migrations.


### Dockerize the Application

1. Build the Docker image:

./gradlew bootBuildImage --imageName=worldline/taskboard:0.0.1-SNAPSHOT

2. Run the Docker container:

docker run -d -p 8080:8080 --link postgres:postgres -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/taskboard worldline/taskboard:0.0.1-SNAPSHOT



### Deploy to AWS

Push Docker Image to ECR:

Create an ECR repository:

aws ecr create-repository --repository-name worldline/taskboard --region <your-region>


Authenticate Docker to ECR:

aws ecr get-login-password --region <your-region> | docker login --username AWS --password-stdin <account-id>.dkr.ecr.<your-region>.amazonaws.com



Tag and push the image:

docker tag worldline/taskboard:0.0.1-SNAPSHOT <account-id>.dkr.ecr.<your-region>.amazonaws.com/worldline/taskboard:0.0.1-SNAPSHOT
docker push <account-id>.dkr.ecr.<your-region>.amazonaws.com/worldline/taskboard:0.0.1-SNAPSHOT



Set Up RDS PostgreSQL:





Create a PostgreSQL RDS instance in AWS.



Update application.properties with the RDS endpoint, username, and password.



Deploy to ECS with Fargate:





Create an ECS cluster:

aws ecs create-cluster --cluster-name taskboard-cluster --region <your-region>



Create a task definition (task-definition.json):

{
    "family": "taskboard",
    "networkMode": "awsvpc",
    "containerDefinitions": [
        {
            "name": "taskboard",
            "image": "<account-id>.dkr.ecr.<your-region>.amazonaws.com/worldline/taskboard:0.0.1-SNAPSHOT",
            "essential": true,
            "portMappings": [
                {
                    "containerPort": 8080,
                    "hostPort": 8080
                }
            ],
            "environment": [
                {
                    "name": "SPRING_DATASOURCE_URL",
                    "value": "jdbc:postgresql://<rds-endpoint>:5432/taskboard"
                },
                {
                    "name": "SPRING_DATASOURCE_USERNAME",
                    "value": "<rds-username>"
                },
                {
                    "name": "SPRING_DATASOURCE_PASSWORD",
                    "value": "<rds-password>"
                }
            ]
        }
    ],
    "requiresCompatibilities": ["FARGATE"],
    "cpu": "256",
    "memory": "512"
}



Register the task definition:

aws ecs register-task-definition --cli-input-json file://task-definition.json --region <your-region>



Create a service:

aws ecs create-service --cluster taskboard-cluster --service-name taskboard-service --task-definition taskboard --desired-count 1 --launch-type FARGATE --network-configuration "awsvpcConfiguration={subnets=[<subnet-id>],securityGroups=[<security-group-id>],assignPublicIp=ENABLED}" --region <your-region>

Developer Onboarding





The project follows a layered architecture (controller, service, repository) for separation of concerns.



Spring Data JDBC is used for persistence, providing a lightweight alternative to JPA.



Liquibase manages the database schema, ensuring consistent migrations across environments.



Swagger UI provides interactive API documentation at /swagger-ui.html.



Integration tests are provided using Testcontainers for realistic database testing.



The application can be run locally with Docker or directly via Gradle.



Deployment to AWS is documented for production use.
