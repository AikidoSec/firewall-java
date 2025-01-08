# Define the base URL and files
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

# Determine the OS
UNAME_S := $(shell uname -s)
# Set defaults :
RM = rm -rf
MKDIR = mkdir -p
CP = cp -r
CURL = curl -L -o

# Define the appropriate commands based on the OS
ifeq ($(UNAME_S),Linux)
    GRADLE = ./gradlew
else ifeq ($(UNAME_S),Darwin)
    GRADLE = ./gradlew
else ifeq ($(findstring MSYS,$(UNAME_S)),MSYS)  # MSYS2 and MinGW environments
    GRADLE = gradlew.bat
else
    $(error Unsupported OS: $(UNAME_S))
endif

# Targets
clean:
	$(RM) dist/
	$(GRADLE) clean

build: clean
	$(MKDIR) dist/
	@echo "Copying binaries from .cache folder"
	$(CP) .cache/binaries dist/binaries
	$(GRADLE) agent:shadowJar
	$(CP) agent/build/libs/agent*-all.jar dist/agent.jar
	$(GRADLE) agent_api:shadowJar
	$(CP) agent_api/build/libs/agent*-all.jar dist/agent_api.jar

mock_init:
	docker kill mock_core && docker rm mock_core
	cd end2end/server && docker build -t mock_core .
	docker run --name mock_core -d -p 5000:5000 mock_core

mock_restart:
	docker restart mock_core

mock_stop:
	docker kill mock_core && docker rm mock_core

test:
	AIKIDO_LOG_LEVEL="error" AIKIDO_TOKEN="token" $(GRADLE) test

cov:
	AIKIDO_LOG_LEVEL="error" AIKIDO_TOKEN="token" $(GRADLE) test --rerun-tasks -PcoverageRun jacocoTestReport

binaries: binaries_make_dir $(addprefix .cache/binaries/, $(FILES))

binaries_make_dir:
	$(RM) .cache/binaries
	$(MKDIR) .cache/binaries/

.cache/binaries/%:
	@echo "Downloading $*..."
	$(CURL) $@ $(BASE_URL)/$*

create_temp_dir_for_gh:
	@echo "This creates /opt/aikido for the github test cases"
	$(MKDIR) /opt/aikido
	chmod 777 /opt/aikido