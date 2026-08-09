# Define variables
JAVA_AGENT = ../../dist/agent.jar

.PHONY: all
all: build

.PHONY: build
build:
	@echo "Resolving dependencies..."
	lein deps

# Run the application with the Java agent
.PHONY: run
run: build
	@echo "Running RingClojurePostgres with Zen (http://localhost:8102)"
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	AIKIDO_AGENT_JAR="$(JAVA_AGENT)" \
	PORT=8102 \
	lein run

# Run the application without Zen
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running RingClojurePostgres without Zen (http://localhost:8103)"
	AIKIDO_TOKEN="random-invalid-token" \
	PORT=8103 \
	lein run

.PHONY: clean
clean:
	lein clean
