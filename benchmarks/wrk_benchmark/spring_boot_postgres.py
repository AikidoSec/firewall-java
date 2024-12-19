from script import run_benchmark

# Run benchmarks

run_benchmark(
    "http://localhost:8080/", # Application with Zen
    "http://localhost:8081/", # Application without Zen
    "An empty route with a 1ms delay (simulation of activity)",
    percentage_limit=65, ms_limit=350
)
