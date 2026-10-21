# Define variables
GRADLEW = ./gradlew
JAR_FILE = build/libs/micronaut-hypersql4-0.1-all.jar
JAVA_AGENT = ../../dist/agent.jar

# Default target
.PHONY: all
all: build

# Build the project (shaded runnable jar)
.PHONY: build
build:
	@echo "Building MicronautHyperSQL4 (Micronaut 4.x)..."
	chmod +x $(GRADLEW)
	$(GRADLEW) shadowJar

# Run the application with the Java agent (http://localhost:8112)
.PHONY: run
run: build
	@echo "Running MicronautHyperSQL4 with Zen (http://localhost:8112)"
	AIKIDO_LOG_LEVEL="error" \
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	java -Dmicronaut.server.port=8112 -javaagent:$(JAVA_AGENT) -jar $(JAR_FILE)

# Run the application without Zen (http://localhost:8113)
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running MicronautHyperSQL4 without Zen (http://localhost:8113)"
	AIKIDO_TOKEN="random-invalid-token" java -Dmicronaut.server.port=8113 -jar $(JAR_FILE)

# Clean the project
.PHONY: clean
clean:
	$(GRADLEW) clean
