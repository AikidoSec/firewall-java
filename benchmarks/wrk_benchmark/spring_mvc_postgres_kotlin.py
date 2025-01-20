from script import run_benchmark

# Run benchmarks

run_benchmark(
    "http://localhost:8092/benchmark", # Application with Zen
    "http://localhost:8093/benchmark", # Application without Zen
    "An empty route (1ms simulated delay)",
    percentage_limit=15, ms_limit=150
)
