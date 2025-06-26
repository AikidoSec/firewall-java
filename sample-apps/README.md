# Java Sample Apps

## Running the apps
1. Make sure you compiled a local version of Zen by running `make build` in the root directory of the project.
2. Start databases by going to [databases/](./databases/) and running :
```bash
docker-compose up --build
```
3. Visit your sample app of choice and run one of the following commands (both use nohup so you'll see no output) :
```shell
make run # This will run with Zen
make runWithoutZen # The port number will be the original + 1.
```

## Overview
Here is an overview of all of our sample apps together with their port numbers.

#### Spring MVC Apps
- [SpringBoot MVC With Postgres](./SpringBootPostgres) on port [`8080`](http://localhost:8080/)
- [SpringBoot MVC With MySQL](./SpringBootMySQL) on port [`8082`](http://localhost:8082/)
- [SpringBoot MVC with Microsoft SQL](./SpringBootMSSQL) on port [`8086`](http://localhost:8086/)
- [SpringBoot MVC with Postgres (Kotlin)](./SpringMVCPostgresKotlin) on port [`8092`](http://localhost:8092/)
- [SpringBoot MVC with Postgres (Groovy)](./SpringMVCPostgresGroovy) on port [`8094`](http://localhost:8094/)
- [SpringBoot MVC With SQLite](./SpringBootSQLite) on port [`8100`](http://localhost:8100/)
- [SpringBoot MVC v2.7 With Postgres](./SpringBoot2.7Postgres) on port [`8104`](http://localhost:8104/)
- [SpringBoot MVC With MongoDB](./SpringBootMongo) on port [`8106`](http://localhost:8106/)

#### Webflux Apps
- [SpringBoot Webflux with Postgres](./SpringWebfluxSampleApp) on port [`8090`](http://localhost:8090/)

#### Other frameworks
- [Javalin App with Postgres](./JavalinPostgres) on port [`8088`](http://localhost:8088/)
- [Javalin App with MySQL (Kotlin)](./JavalinMySQLKotlin/) on port [`8098`](http://localhost:8098)
- [Ktor app with Postgres (Netty)](./KtorPostgresNetty) on port [`8096`](http://localhost:8096)

#### Clojure
- [Ring app with Postgres](./RingClojurePostgres) on port [`8102`](http://localhost:8102/)
