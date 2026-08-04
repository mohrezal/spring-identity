#!/usr/bin/env bash

set -euo pipefail

if [ -z "${1:-}" ] || [[ "${1:-}" == --* ]]; then
    echo "USAGE: ./scripts/new-migration.sh <migration_name> [--split-statements=true|false]"
    exit 1
fi

MIGRATION_NAME="$1"
shift

if [[ ! "${MIGRATION_NAME}" =~ ^[a-z0-9_]+$ ]]; then
    echo "Migration name must contain only lowercase letters, digits, and underscores."
    exit 1
fi

SPLIT_STATEMENTS="false"
TIMESTAMP=$(date +%s)
CHANGE_SET="${TIMESTAMP}_${MIGRATION_NAME}"
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPOSITORY_ROOT=$(cd "${SCRIPT_DIR}/.." && pwd)
MIGRATION_DIR="${REPOSITORY_ROOT}/services/identity-api/src/main/resources/db/changelog/migrations"
MIGRATION_FILE="${MIGRATION_DIR}/${CHANGE_SET}.sql"

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

if [[ -e "${MIGRATION_FILE}" ]]; then
    echo "Migration already exists: ${MIGRATION_FILE}"
    exit 1
fi

cat > "${MIGRATION_FILE}" << EOF
--liquibase formatted sql
--changeset $(whoami):${CHANGE_SET} splitStatements:${SPLIT_STATEMENTS}
-- Your SQL here

--rollback -- Your rollback SQL here
EOF

echo "✅ Created: ${MIGRATION_FILE}"
echo "Add the migration to services/identity-api/src/main/resources/db/changelog/db.changelog-master.yml"
