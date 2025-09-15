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
	@echo "Running SpringBoot2.7Postgres with Zen & ENV (http://localhost:8104)"
	AIKIDO_LOG_LEVEL="error" \
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	java -javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) --server.port=8104

.PHONY: runWithDdTrace
runWithDdTrace: build
	@echo "Running SpringBoot2.7Postgres with Zen, ddtrace & ENV (http://localhost:8104)"
	wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'
	AIKIDO_LOG_LEVEL="error" \
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	java \
		-javaagent:dd-java-agent.jar -Ddd.profiling.enabled=true -Ddd.logs.injection=true -Ddd.service=my-app -Ddd.env=staging -Ddd.version=1.0 \
		-javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) --server.port=8104

# Run the application without Zen
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running SpringBoot2.7Postgres without Zen & ENV (http://localhost:8105)"
	AIKIDO_TOKEN="random-invalid-token" java -jar $(JAR_FILE) --server.port=8105

# Clean the project
.PHONY: clean
clean:
	@echo "Cleaning the project..."
	$(GRADLEW) clean
