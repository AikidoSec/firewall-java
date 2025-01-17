from script import run_benchmark

# Run benchmarks for Spring Webflux Postgres

run_benchmark(
    "http://localhost:8090/benchmark_empty_route", # Application with Zen
    "http://localhost:8091/benchmark_empty_route", # Application without Zen
    "An empty route",
    percentage_limit=50, ms_limit=500
)
