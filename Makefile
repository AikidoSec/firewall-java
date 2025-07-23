clean:
	rm -rf dist/
	./gradlew clean

build: clean check_binaries
	mkdir -p dist/

	@echo "Copying binaries from .cache folder"
	cp -r .cache/binaries dist/binaries

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

test: check_binaries
	AIKIDO_LOG_LEVEL="error" AIKIDO_TOKEN="token" ./gradlew test

cov: check_binaries
	AIKIDO_LOG_LEVEL="error" AIKIDO_TOKEN="token" ./gradlew test --rerun-tasks -PcoverageRun jacocoTestReport


# Binaries :

BASE_URL = https://github.com/AikidoSec/zen-internals/releases/download/v0.1.37
FILES = \
    libzen_internals_aarch64-apple-darwin.dylib \
    libzen_internals_aarch64-apple-darwin.dylib.sha256sum \
    libzen_internals_aarch64-unknown-linux-gnu.so \
    libzen_internals_aarch64-unknown-linux-gnu.so.sha256sum \
	libzen_internals_aarch64-unknown-linux-musl.so \
	libzen_internals_aarch64-unknown-linux-musl.so.sha256sum \
    libzen_internals_x86_64-apple-darwin.dylib \
    libzen_internals_x86_64-apple-darwin.dylib.sha256sum \
    libzen_internals_x86_64-pc-windows-gnu.dll \
    libzen_internals_x86_64-pc-windows-gnu.dll.sha256sum \
    libzen_internals_x86_64-unknown-linux-gnu.so \
    libzen_internals_x86_64-unknown-linux-gnu.so.sha256sum \
    libzen_internals_x86_64-unknown-linux-musl.so \
	libzen_internals_x86_64-unknown-linux-musl.so.sha256sum \

binaries: binaries_make_dir $(addprefix .cache/binaries/, $(FILES))
binaries_make_dir:
	rm -rf .cache/binaries
	mkdir -p .cache/binaries/
.cache/binaries/%:
	@echo "Downloading $*..."
	curl -L -o $@ $(BASE_URL)/$*
.PHONY: check_binaries
check_binaries:
	@if [ -d ".cache/binaries" ]; then \
  		echo "Cache directory exists."; \
	else \
		echo "Cache directory is empty. Running 'make binaries'..."; \
		$(MAKE) binaries; \
	fi


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

