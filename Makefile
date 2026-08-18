clean:
	rm -rf dist/
	./gradlew clean

ZEN_INTERNALS_VERSION = v0.1.60
WASM_BASE_URL = https://github.com/AikidoSec/zen-internals/releases/download/$(ZEN_INTERNALS_VERSION)
WASM_RESOURCE_DIR = agent_api/src/main/resources

.PHONY: wasm download-wasm check-wasm
wasm: download-wasm check-wasm

download-wasm:
	mkdir -p $(WASM_RESOURCE_DIR)
	curl -fL -o $(WASM_RESOURCE_DIR)/zen_internals.wasm $(WASM_BASE_URL)/libzen_internals.wasm
	curl -fL $(WASM_BASE_URL)/libzen_internals.wasm.sha256sum \
		| sed 's/libzen_internals\.wasm/zen_internals.wasm/' \
		> $(WASM_RESOURCE_DIR)/zen_internals.wasm.sha256sum

check-wasm:
	@expected=$$(awk '{print $$1}' $(WASM_RESOURCE_DIR)/zen_internals.wasm.sha256sum); \
	actual=$$(shasum -a 256 $(WASM_RESOURCE_DIR)/zen_internals.wasm | awk '{print $$1}'); \
	if [ "$$expected" != "$$actual" ]; then \
		echo "WASM checksum mismatch: expected $$expected, got $$actual"; \
		exit 1; \
	fi

build: clean
	mkdir -p dist/

	./gradlew agent:shadowJar
	cp agent/build/libs/agent*-all.jar dist/agent.jar

	./gradlew agent_api:shadowJar
	cp agent_api/build/libs/agent*-all.jar dist/agent_api.jar

mock_init:
	docker kill mock_core && docker rm mock_core
	cd end2end/server && docker build -t mock_core .
	docker run --name mock_core -d -p 5000:5000 mock_core
mock_restart:
	docker restart mock_core
mock_stop:
	docker kill mock_core && docker rm mock_core

test:
	AIKIDO_LOG_LEVEL="error" AIKIDO_TOKEN="token" ./gradlew test

cov:
	AIKIDO_LOG_LEVEL="error" AIKIDO_TOKEN="token" ./gradlew test --rerun-tasks -PcoverageRun jacocoTestReport jacocoTestCoverageVerification


# Automatic versioning for releases :

VERSION_FILES = ./build.gradle ./agent_api/src/main/java/dev/aikido/agent_api/Config.java
replace_version:
	@if [ -z "$(version)" ]; then \
		echo "Error: No version specified. Use 'make replace-version version=<new_version>'."; \
		exit 1; \
	fi;

	@for file in $(VERSION_FILES); do \
		echo "Updating $$file with version $(version)"; \
		sed -i.bak "s/1.0-REPLACE-VERSION/$$version/g" $$file; \
		rm $$file.bak; \
	done;
