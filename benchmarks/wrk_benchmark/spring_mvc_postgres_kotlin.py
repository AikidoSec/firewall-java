from script import run_benchmark

# Run benchmarks

run_benchmark(
    "http://localhost:8092/benchmark_empty_route", # Application with Zen
    "http://localhost:8093/benchmark_empty_route", # Application without Zen
    "An empty route",
    percentage_limit=15, ms_limit=150
)
