from script import run_benchmark

# Run benchmarks for Spring Webflux Postgres

run_benchmark(
    "http://localhost:8090/benchmark",  # Application with Zen
    "http://localhost:8091/benchmark",  # Application without Zen
    "An empty route (1ms simulated delay)",
    percentage_limit=20, ms_limit=100
)
