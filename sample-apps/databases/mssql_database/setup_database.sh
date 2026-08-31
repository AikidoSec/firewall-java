#!/usr/bin/env bash

for attempt in {1..60}; do
    if /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "SELECT 1" -N -C >/dev/null 2>&1; then
        exec /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -i /setup.sql -N -C -b
    fi
    sleep 2
done

echo "SQL Server did not become ready in time" >&2
exit 1
