from script import run_benchmark

# Run benchmarks

run_benchmark(
    "http://localhost:8080/", # Application with Zen
    "http://localhost:8081/", # Application without Zen
    "An empty route that returns a small HTML file",
    percentage_limit=65, ms_limit=200
)
