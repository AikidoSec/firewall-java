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
	@echo "Running SpringBootMSSQL with Zen & ENV (http://localhost:8086)"
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCKING=1 \
	nohup java -javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) --server.port=8086 > output1.log &

# Run the application without Zen
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running SpringBootMSSQL without Zen & ENV (http://localhost:8087)"
	AIKIDO_TOKEN="random-invalid-token" \
	nohup java -jar $(JAR_FILE) --server.port=8087 > output2.log &

# Clean the project
.PHONY: clean
clean:
	@echo "Cleaning the project..."
	$(GRADLEW) clean

.PHONY: kill
kill:
	pkill -f java
