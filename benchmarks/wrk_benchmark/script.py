import re
import subprocess
import sys
import time

def generate_wrk_command_for_url(url):
    # Define the command with awk included
    return "wrk -t12 -c400 -d15s " + url

def cold_start(url):
    for i in range(10):
        subprocess.run(
            "curl " + url,
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )

def extract_requests_and_latency_tuple(output):
    if output.returncode == 0:
        # Extracting requests/sec
        requests_sec = re.search(r'Requests/sec:\s+([\d.]+)', output.stdout).group(1)
        # Extracting latency
        latency = re.search(r'Latency\s+([\d.]+)(ms|s)', output.stdout)
        latency_float = float(latency.group(1))
        if latency.group(2) == "s":
            latency_float *= 1000
        return (float(requests_sec), latency_float)
    else:
        print("Error occured running benchmark command:")
        print(output.stderr.strip())
        sys.exit(1)

def run_benchmark(route1, route2, descriptor, percentage_limit, ms_limit):
    # Cold start :
    cold_start(route1)
    cold_start(route2)

    output_nofw = subprocess.run(
        generate_wrk_command_for_url(route2),
        shell=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    time.sleep(5)
    output_fw = subprocess.run(
        generate_wrk_command_for_url(route1),
        shell=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    result_nofw = extract_requests_and_latency_tuple(output_nofw)
    result_fw = extract_requests_and_latency_tuple(output_fw)


    # Check if the command was successful
    if result_nofw and result_fw:
        # Print the output, which should be the Requests/sec value
        print(f"[FIREWALL-ON] Requests/sec: {result_fw[0]} | Latency in ms: {result_fw[1]}")
        print(f"[FIREWALL-OFF] Requests/sec: {result_nofw[0]} | Latency in ms: {result_nofw[1]}")

        delta_in_ms = round(result_fw[1] - result_nofw[1], 2)
        print(f"-> Delta in ms: {delta_in_ms}ms after running load test on {descriptor}")

        delay_percentage = round(
            (result_nofw[0] - result_fw[0]) / result_nofw[0] * 100
        )
        print(
            f"-> {delay_percentage}% decrease in throughput after running load test on {descriptor} \n"
        )

        if delta_in_ms > ms_limit:
            sys.exit(1)
        if delay_percentage > percentage_limit:
            sys.exit(1)