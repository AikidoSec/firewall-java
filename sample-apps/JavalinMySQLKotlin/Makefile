# Define variables
JAVA_AGENT = ../../dist/agent.jar
JAR_FILE = target/JavalinMySQLKotlin-1.0-SNAPSHOT.jar

# Build the project
.PHONY: build
build:
	@echo "Building the project..."
	mvn install:install-file -Dfile=../../dist/agent_api.jar -DgroupId=dev.aikido -DartifactId=agent_api -Dversion=1.0 -Dpackaging=jar
	mvn clean package

# Run the application with the Java agent
.PHONY: run
run: build
	@echo "Running JavalinMySQLKotlin with Zen & ENV (http://localhost:8098)"
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCKING=1 \
	PORT=8098 nohup java -javaagent:$(JAVA_AGENT) -jar $(JAR_FILE) > output1.log &

# Run the application without Zen
.PHONY: runWithoutZen
runWithoutZen:
	@echo "Running JavalinMySQLKotlin without Zen & ENV (http://localhost:8099)"
	AIKIDO_TOKEN="random-invalid-token" \
	PORT=8099 nohup java -jar $(JAR_FILE) > output2.log &

.PHONY: kill
kill:
	pkill -f java
