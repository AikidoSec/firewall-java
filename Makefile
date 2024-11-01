clean:
	rm -rf dist/
	./gradlew clean

build: clean
	mkdir -p dist/
	./gradlew agent:shadowJar
	cp agent/build/libs/agent*-all.jar dist/agent.jar

	./gradlew agent_api:shadowJar
	cp agent_api/build/libs/agent*-all.jar dist/agent_api.jar