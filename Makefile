clean:
	rm -rf dist/
	./gradlew clean

build: clean
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

test:
	AIKIDO_LOG_LEVEL="trace" AIKIDO_TOKEN="token" ./gradlew test --tests "thread_cache.ThreadCacheRenewalTest"

cov:
	AIKIDO_LOG_LEVEL="error" AIKIDO_TOKEN="token" ./gradlew test --rerun-tasks -PcoverageRun jacocoTestReport

BASE_URL = https://github.com/AikidoSec/zen-internals/releases/download/v0.1.26
FILES = \
    libzen_internals_aarch64-apple-darwin.dylib \
    libzen_internals_aarch64-apple-darwin.dylib.sha256sum \
    libzen_internals_aarch64-unknown-linux-gnu.so \
    libzen_internals_aarch64-unknown-linux-gnu.so.sha256sum \
    libzen_internals_x86_64-apple-darwin.dylib \
    libzen_internals_x86_64-apple-darwin.dylib.sha256sum \
    libzen_internals_x86_64-pc-windows-gnu.dll \
    libzen_internals_x86_64-pc-windows-gnu.dll.sha256sum \
    libzen_internals_x86_64-unknown-linux-gnu.so \
    libzen_internals_x86_64-unknown-linux-gnu.so.sha256sum

binaries: binaries_make_dir $(addprefix .cache/binaries/, $(FILES))
binaries_make_dir:
	rm -rf .cache/binaries
	mkdir -p .cache/binaries/
.cache/binaries/%:
	@echo "Downloading $*..."
	curl -L -o $@ $(BASE_URL)/$*
