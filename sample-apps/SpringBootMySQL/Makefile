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
	@echo "Running SpringBootMySQL with Zen & ENV (http://localhost:8082)"
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	AIKIDO_LOG_LEVEL="debug" \
	java -javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) --server.port=8082

# Run the application without Zen
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running SpringBootMySQL without Zen & ENV (http://localhost:8083)"
	AIKIDO_TOKEN="random-invalid-token" java -jar $(JAR_FILE) --server.port=8083

# Clean the project
.PHONY: clean
clean:
	@echo "Cleaning the project..."
	$(GRADLEW) clean
