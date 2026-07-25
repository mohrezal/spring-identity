#!/usr/bin/env bash
set -euo pipefail

BASE="http://localhost:8080"
TMP=$(mktemp -d)
COOKIE_JAR="$TMP/cookies.txt"

csrf() {
  curl -s -c "$COOKIE_JAR" "$BASE/api/auth/csrf" | jq -r '.token'
}

req() {
  local method=$1 path=$2 data=$3
  local token
  token=$(csrf)
  STATUS=$(curl -s -b "$COOKIE_JAR" -o /dev/null -w '%{http_code}' \
    -X "$method" "$BASE$path" \
    -H "Content-Type: application/json" \
    -H "X-XSRF-TOKEN: $token" \
    -d "$data" 2>/dev/null || echo "ERR")
  echo "$STATUS"
}

echo "==> 1. Successful registrations..."
for i in $(seq 1 3); do
  user="user$i@test.com"
  echo -n "  $user → "
  STATUS=$(req POST "/api/users/register?redirectUrl=http://localhost:3000" \
    "{\"firstName\":\"Test$i\",\"lastName\":\"User$i\",\"email\":\"$user\",\"password\":\"ValidPass123!\"}")
  echo "$STATUS"
done

echo ""
echo "==> 2. Failed registrations (invalid data)..."
echo -n "  missing password → "
STATUS=$(req POST "/api/users/register?redirectUrl=http://localhost:3000" \
  '{"firstName":"Bad","lastName":"User","email":"bad@test.com"}')
echo "$STATUS"

echo -n "  invalid email → "
STATUS=$(req POST "/api/users/register?redirectUrl=http://localhost:3000" \
  '{"firstName":"Bad","lastName":"User","email":"not-an-email","password":"ValidPass123!"}')
echo "$STATUS"

echo ""
echo "==> 3. Logins..."
for i in $(seq 1 3); do
  echo -n "  user$i@test.com → "
  STATUS=$(req POST "/api/auth/login" \
    "{\"email\":\"user$i@test.com\",\"password\":\"ValidPass123!\"}")
  echo "$STATUS"
done

echo -n "  wrong password → "
STATUS=$(req POST "/api/auth/login" \
  '{"email":"user1@test.com","password":"WrongPass123!"}')
echo "$STATUS"

echo -n "  unknown user → "
STATUS=$(req POST "/api/auth/login" \
  '{"email":"nonexistent@test.com","password":"SomePass123!"}')
echo "$STATUS"

echo ""
echo "==> 4. Password reset requests..."
echo -n "  user1@test.com → "
STATUS=$(req POST "/api/auth/forgot-password?redirectUrl=http://localhost:3000/reset" \
  '{"email":"user1@test.com"}')
echo "$STATUS"

echo -n "  unknown@test.com → "
STATUS=$(req POST "/api/auth/forgot-password?redirectUrl=http://localhost:3000/reset" \
  '{"email":"unknown@test.com"}')
echo "$STATUS"

echo ""
echo "==> 5. Resend email verification..."
echo -n "  user1@test.com → "
STATUS=$(req POST "/api/auth/resend-email-verification?redirectUrl=http://localhost:3000" \
  '{"email":"user1@test.com"}')
echo "$STATUS"

echo ""
echo "==> 6. Rapid requests (rate limit trigger)..."
echo -n "  sending 55 login attempts... "
for i in $(seq 1 55); do
  (req POST "/api/auth/login" '{"email":"hammer@test.com","password":"WrongPass123!"}' > /dev/null 2>&1) &
done
wait
echo "done (check for 429s above)"

rm -rf "$TMP"
echo ""
echo "=== Done ==="
