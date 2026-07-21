#!/usr/bin/env python3

import json
import sys
from pathlib import Path
from typing import Any

from jsonschema import Draft202012Validator
from jsonschema.exceptions import SchemaError


def load_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as error:
        print(
            f"FAIL {path}: invalid JSON syntax at "
            f"line {error.lineno}, column {error.colno}: {error.msg}"
        )
        raise


def validate_semantics(document: Any) -> list[str]:
    errors: list[str] = []

    if not isinstance(document, dict):
        return errors

    trace_id = document.get("traceId")
    request = document.get("request")

    if isinstance(request, dict):
        request_id = request.get("requestId")

        if request_id != trace_id:
            errors.append("$.request.requestId must equal $.traceId")

    return errors


def format_schema_error(error: Any) -> str:
    if error.absolute_path:
        location = "$." + ".".join(
            str(part) for part in error.absolute_path
        )
    else:
        location = "$"

    return f"{location}: {error.message}"


def validate_valid_examples(
    validator: Draft202012Validator,
    examples_dir: Path,
) -> tuple[int, int]:
    failures = 0
    files = sorted(examples_dir.glob("*.json"))

    print("Valid examples")

    if not files:
        print("  FAIL no valid examples found")
        return 1, 0

    for path in files:
        try:
            document = load_json(path)
        except json.JSONDecodeError:
            failures += 1
            continue

        schema_errors = sorted(
            validator.iter_errors(document),
            key=lambda error: list(error.absolute_path),
        )

        semantic_errors = (
            validate_semantics(document)
            if not schema_errors
            else []
        )

        if schema_errors or semantic_errors:
            failures += 1
            print(f"  FAIL {path.name}")

            for error in schema_errors:
                print(f"       {format_schema_error(error)}")

            for error in semantic_errors:
                print(f"       {error}")
        else:
            print(f"  PASS {path.name}")

    return failures, len(files)


def validate_invalid_examples(
    validator: Draft202012Validator,
    invalid_examples_dir: Path,
) -> tuple[int, int]:
    failures = 0
    files = sorted(invalid_examples_dir.glob("*.json"))

    print("Invalid examples")

    if not files:
        print("  FAIL no invalid examples found")
        return 1, 0

    for path in files:
        try:
            document = load_json(path)
        except json.JSONDecodeError:
            print(f"  PASS {path.name} rejected due to invalid JSON")
            continue

        schema_errors = list(validator.iter_errors(document))

        semantic_errors = (
            validate_semantics(document)
            if not schema_errors
            else []
        )

        if schema_errors or semantic_errors:
            print(f"  PASS {path.name} rejected")
        else:
            failures += 1
            print(f"  FAIL {path.name} was unexpectedly accepted")

    return failures, len(files)


def main() -> int:
    if len(sys.argv) != 4:
        print(
            "Usage: validate-audit-contract.py "
            "<schema-file> <examples-dir> <invalid-examples-dir>",
            file=sys.stderr,
        )
        return 2

    schema_path = Path(sys.argv[1])
    examples_dir = Path(sys.argv[2])
    invalid_examples_dir = Path(sys.argv[3])

    try:
        schema = load_json(schema_path)
        Draft202012Validator.check_schema(schema)
    except (json.JSONDecodeError, SchemaError) as error:
        print(f"FAIL schema: {error}")
        return 1

    validator = Draft202012Validator(
        schema,
        format_checker=Draft202012Validator.FORMAT_CHECKER,
    )

    print("Schema")
    print(f"  PASS {schema_path.name}")
    print()

    valid_failures, valid_count = validate_valid_examples(
        validator,
        examples_dir,
    )

    print()

    invalid_failures, invalid_count = validate_invalid_examples(
        validator,
        invalid_examples_dir,
    )

    print()

    failures = valid_failures + invalid_failures

    if failures:
        print(f"Contract validation failed with {failures} error(s).")
        return 1

    print(
        "Contract validation passed: "
        f"{valid_count} valid example(s), "
        f"{invalid_count} invalid example(s)."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())