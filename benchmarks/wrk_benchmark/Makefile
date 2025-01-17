WRK_CHECK = wrk --version
WRK_INSTALL = sudo apt-get install -y wrk

# Target to check and install wrk
.PHONY: check_wrk
check_wrk:
	@command -v wrk >/dev/null 2>&1 || { \
		echo "wrk not found, installing..."; \
		$(WRK_INSTALL); \
	}

.PHONY: benchmark_SpringBootPostgres
benchmark_SpringBootPostgres: check_wrk
	@echo "Running Python benchmarks for SpringBootPostgres"
	python spring_boot_postgres.py

