# Define variables
GRADLEW = ./gradlew
JAR_FILE = build/libs/JavalinPostgres-1.0-SNAPSHOT-all.jar
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
	@echo "Running JavalinPostgres with Zen & ENV (http://localhost:8088)"
	AIKIDO_TOKEN="AIK_RUNTIME_644_29096_zsgZKC6o5eGiM8AHV5PsfL3TDF01RuIcIe5HBTF7ftVLoLpb" \
	AIKIDO_BLOCK=1 \
	java -javaagent:$(JAVA_AGENT) -DportNumber=8088 -jar $(JAR_FILE)

# Run the application without Zen
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running JavalinPostgres without Zen & ENV (http://localhost:8089)"
	AIKIDO_TOKEN="random-invalid-token" java -DportNumber=8089 -jar $(JAR_FILE)

# Test with opentelemetry :
.PHONY: runWithOpentel
runWithOpentel: build
	@echo "Running JavalinPostgres with Zen & OpenTelemetry"
	wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	AIKIDO_LOG_LEVEL="trace" \
	java -javaagent:$(JAVA_AGENT) -javaagent:opentelemetry-javaagent.jar -Dotel.service.name=service -DportNumber=8088 -jar $(JAR_FILE)

# Clean the project
.PHONY: clean
clean:
	@echo "Cleaning the project..."
	$(GRADLEW) clean
