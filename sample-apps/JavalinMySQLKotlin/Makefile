# Define variables
JAVA_AGENT = ../../dist/agent.jar

# Build the project
.PHONY: build
build:
	@echo "Building the project..."
	mvn clean compile

# Run the application with the Java agent
.PHONY: run
run: build
	@echo "Running JavalinMySQLKotlin with Zen & ENV (http://localhost:8098)"
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCKING=1 MAVEN_OPTS="-javaagent:$(JAVA_AGENT)" \
	PORT=8098 nohup mvn exec:java -Dexec.mainClass="MainKt" > output1.log &

# Run the application without Zen
.PHONY: runWithoutZen
runWithoutZen: build
	@echo "Running JavalinMySQLKotlin without Zen & ENV (http://localhost:8099)"
	AIKIDO_TOKEN="random-invalid-token" \
	PORT=8099 nohup mvn exec:java -Dexec.mainClass="MainKt" > output2.log &

.PHONY: kill
kill:
	pkill -f java
