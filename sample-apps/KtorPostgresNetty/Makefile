# Define variables
GRADLEW = ./gradlew
JAR_FILE = build/libs/ktor-sample-all.jar
JAVA_AGENT = ../../dist/agent.jar

# Default target
.PHONY: all
all: build

# Build the project
.PHONY: build
build:
	@echo "Building the project..."
	chmod +x $(GRADLEW)
	$(GRADLEW) shadowJar

# Run the application with the Java agent
.PHONY: run
run: build
	@echo "Running KtorPostgresNetty with Zen & ENV (http://localhost:8096)"
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	java -javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) 8096

# Run the application without Zen
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running KtorPostgresNetty without Zen & ENV (http://localhost:8097)"
	AIKIDO_TOKEN="random-invalid-token" java -jar $(JAR_FILE) 8097

# Clean the project
.PHONY: clean
clean:
	@echo "Cleaning the project..."
	$(GRADLEW) clean
