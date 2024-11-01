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

test:
	AIKIDO_DIRECTORY="$(shell pwd)/dist" ./gradlew test


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