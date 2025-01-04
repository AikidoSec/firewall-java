PYTHON_SCRIPT_SB_POSTGRES = spring_boot_postgres.py

WRK_CHECK = wrk --version
WRK_INSTALL = sudo apt-get install -y wrk

# Target to check and install wrk
.PHONY: check_wrk
check_wrk:
	@command -v wrk >/dev/null 2>&1 || { \
		echo "wrk not found, installing..."; \
		$(WRK_INSTALL); \
	}

# Target to run the Python benchmark script
.PHONY: sb_postgres
sb_postgres: check_wrk
	@echo "Running the Python benchmark script..."
	python $(PYTHON_SCRIPT_SB_POSTGRES)

