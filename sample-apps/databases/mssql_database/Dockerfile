FROM mcr.microsoft.com/mssql/server:2019-latest
USER root

COPY setup.sql ./setup.sql
COPY setup_database.sh ./setup_database.sh
COPY entrypoint.sh ./entrypoint.sh
RUN chmod +x entrypoint.sh
RUN chmod +x setup_database.sh

# Set the entrypoint
RUN ls /opt/
CMD /bin/bash ./entrypoint.sh
