#!/bin/sh
# wait-for-postgres.sh

set -e
  
host="$DATABASE_URL"
shift
cmd="$@"
  
until PGPASSWORD=$POSTGRES_PASSWORD psql "$host" -c '\q'; do
  >&2 echo "Postgres is unavailable - sleeping"
  sleep 1
done
  
>&2 echo "Postgres is up - executing command"

psql -c '\i words.sql' -d "$host" -U postgres

exec $cmd