# Define variables
JAVA_AGENT = ../../dist/agent.jar

# Run the application with the Java agent
.PHONY: run
run:
	@echo "Running RingClojurePostgres with Zen (http://localhost:8102)"
	AIKIDO_TOKEN="token" \
	AIKIDO_REALTIME_ENDPOINT="http://localhost:5000/realtime" \
	AIKIDO_ENDPOINT="http://localhost:5000" \
	AIKIDO_BLOCK=1 \
	nohup lein run > output1.log &
