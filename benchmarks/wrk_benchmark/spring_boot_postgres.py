from script import run_benchmark

# Run benchmarks

run_benchmark(
    "http://localhost:8080/benchmark", # Application with Zen
    "http://localhost:8081/benchmark", # Application without Zen
    "An empty route (Simulated 1ms delay)",
    percentage_limit=15, ms_limit=150
)
