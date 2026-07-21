#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CONTRACT_ROOT="${PROJECT_ROOT}/contracts/audit/v1"

SCHEMA_FILE="${CONTRACT_ROOT}/audit-event.schema.json"
EXAMPLES_DIR="${CONTRACT_ROOT}/examples"
INVALID_EXAMPLES_DIR="${CONTRACT_ROOT}/invalid-examples"
VALIDATOR_SCRIPT="${PROJECT_ROOT}/scripts/validate-audit-contract.py"

if [[ -x "${PROJECT_ROOT}/.venv/bin/python" ]]; then
  PYTHON="${PROJECT_ROOT}/.venv/bin/python"
else
  PYTHON="python3"
fi

if [[ ! -f "${SCHEMA_FILE}" ]]; then
  echo "ERROR: Schema file not found: ${SCHEMA_FILE}" >&2
  exit 1
fi

if [[ ! -d "${EXAMPLES_DIR}" ]]; then
  echo "ERROR: Examples directory not found: ${EXAMPLES_DIR}" >&2
  exit 1
fi

if [[ ! -d "${INVALID_EXAMPLES_DIR}" ]]; then
  echo "ERROR: Invalid examples directory not found: ${INVALID_EXAMPLES_DIR}" >&2
  exit 1
fi

if [[ ! -f "${VALIDATOR_SCRIPT}" ]]; then
  echo "ERROR: Validator script not found: ${VALIDATOR_SCRIPT}" >&2
  exit 1
fi

if ! "${PYTHON}" -c "import jsonschema" >/dev/null 2>&1; then
  echo "ERROR: Python package 'jsonschema' is not installed." >&2
  echo "Install it with:" >&2
  echo "  ${PYTHON} -m pip install 'jsonschema[format]'" >&2
  exit 1
fi

exec "${PYTHON}" \
  "${VALIDATOR_SCRIPT}" \
  "${SCHEMA_FILE}" \
  "${EXAMPLES_DIR}" \
  "${INVALID_EXAMPLES_DIR}"