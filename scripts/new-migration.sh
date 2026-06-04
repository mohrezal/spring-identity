#!/bin/bash

if [ -z "$1" ] || [[ "$1" == --* ]]; then
    echo "USAGE: ./new-migration.sh <migration_name> [--split-statements=true|false]"
    exit 1
fi

MIGRATION_NAME="$1"
shift

SPLIT_STATEMENTS="false"
TIMESTAMP=$(date +%s)
CHANGE_SET="${TIMESTAMP}_${MIGRATION_NAME}"
MIGRATION_DIR="src/main/resources/db/changelog/migrations"


for arg in "$@"; do
    case "$arg" in
        --split-statements=true|--split-statements=false)
            SPLIT_STATEMENTS="${arg#--split-statements=}"
            ;;
        *)
            echo "❌ Unknown or invalid argument: '${arg}'"
            echo "   Accepted: --split-statements=true|false"
            exit 1
            ;;
    esac
done

mkdir -p "${MIGRATION_DIR}"

cat > "${MIGRATION_DIR}/${CHANGE_SET}.sql" << EOF
--liquibase formatted sql
--changeset $(whoami):${CHANGE_SET} splitStatements:${SPLIT_STATEMENTS}
-- Your SQL here

--rollback -- Your rollback SQL here
EOF

echo "✅ Created: ${MIGRATION_DIR}/${CHANGE_SET}.sql"