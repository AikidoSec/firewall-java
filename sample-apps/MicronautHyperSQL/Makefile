# Define variables
GRADLEW = ./gradlew
JAR_FILE = build/libs/micronaut-hypersql-0.1-all.jar
JAVA_AGENT = ../../dist/agent.jar

# Default target
.PHONY: all
all: build

# Build the project (shaded runnable jar)
.PHONY: build
build:
	@echo "Building MicronautHyperSQL..."
	chmod +x $(GRADLEW)
	$(GRADLEW) shadowJar

# Run the application with the Java agent (http://localhost:8110)
.PHONY: run
run: build
	@echo "Running MicronautHyperSQL with Zen (http://localhost:8110)"
	AIKIDO_LOG_LEVEL="error" \
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	java -Dmicronaut.server.port=8110 -javaagent:$(JAVA_AGENT) -jar $(JAR_FILE)

# Run the application without Zen (http://localhost:8111)
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running MicronautHyperSQL without Zen (http://localhost:8111)"
	AIKIDO_TOKEN="random-invalid-token" java -Dmicronaut.server.port=8111 -jar $(JAR_FILE)

# Clean the project
.PHONY: clean
clean:
	$(GRADLEW) clean
