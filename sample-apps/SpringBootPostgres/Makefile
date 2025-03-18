# Define variables
GRADLEW = ./gradlew
JAR_FILE = build/libs/demo-0.0.1-SNAPSHOT.jar
JAVA_AGENT = ../../dist/agent.jar

# Default target
.PHONY: all
all: build

# Build the project
.PHONY: build
build:
	@echo "Building the project..."
	chmod +x $(GRADLEW)
	$(GRADLEW) build

# Run the application with the Java agent
.PHONY: run
run: build
	@echo "Running SpringBootPostgres with Zen & ENV (http://localhost:8080)"
	AIKIDO_LOG_LEVEL="error" \
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	nohup java -javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) --server.port=8080 > output1.log &

.PHONY: runWithDdTrace
runWithDdTrace: build
	@echo "Running SpringBootPostgres with Zen, ddtrace & ENV (http://localhost:8080)"
	wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'
	AIKIDO_LOG_LEVEL="error" \
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	nohup java -javaagent:dd-java-agent.jar -Ddd.profiling.enabled=true -Ddd.logs.injection=true -Ddd.service=my-app -Ddd.env=staging -Ddd.version=1.0 -javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) --server.port=8080 > output1.log &

# Run the application without Zen
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running SpringBootPostgres without Zen & ENV (http://localhost:8081)"
	AIKIDO_TOKEN="random-invalid-token" \
	nohup java -jar $(JAR_FILE) --server.port=8081 > output2.log &

# Clean the project
.PHONY: clean
clean:
	@echo "Cleaning the project..."
	$(GRADLEW) clean

.PHONY: kill
kill:
	pkill -f java
