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
	java -javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) --server.port=8080

.PHONY: runWithDdTrace
runWithDdTrace: build
	@echo "Running SpringBootPostgres with Zen, ddtrace & ENV (http://localhost:8080)"
	wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'
	AIKIDO_LOG_LEVEL="error" \
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 java \
		-javaagent:dd-java-agent.jar -Ddd.profiling.enabled=true -Ddd.logs.injection=true -Ddd.service=my-app -Ddd.env=staging -Ddd.version=1.0 \
		-javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) --server.port=8080

# Run with the OpenTelemetry agent loaded before Zen (the order that broke Zen instrumentation).
# The agent runs as a -javaagent on the runner, so pin the version and verify its checksum.
OTEL_VERSION = 2.30.0
OTEL_SHA256 = 9d6bc2ad8dd8fb7f730984988e57b8ac0a82d81c7b3b8ae795378718733a509d
.PHONY: runWithOpentel
runWithOpentel: build
	@echo "Running SpringBootPostgres with OpenTelemetry $(OTEL_VERSION) (first) & Zen (http://localhost:8080)"
	wget -O opentelemetry-javaagent.jar 'https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v$(OTEL_VERSION)/opentelemetry-javaagent.jar'
	echo "$(OTEL_SHA256)  opentelemetry-javaagent.jar" | sha256sum -c -
	AIKIDO_LOG_LEVEL="error" \
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 java \
		-javaagent:opentelemetry-javaagent.jar -Dotel.service.name=service -Dotel.traces.exporter=none -Dotel.metrics.exporter=none -Dotel.logs.exporter=none \
		-javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) --server.port=8080

# Run the application without Zen
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running SpringBootPostgres without Zen & ENV (http://localhost:8081)"
	AIKIDO_TOKEN="random-invalid-token" java -jar $(JAR_FILE) --server.port=8081

# Clean the project
.PHONY: clean
clean:
	@echo "Cleaning the project..."
	$(GRADLEW) clean
