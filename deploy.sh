#!/usr/bin/env bash

set -euo pipefail

REMOTE_HOST="root@korean.inaus.co.kr"
REMOTE_JAR_PATH="/home/team/team2.jar"
REMOTE_LOG_PATH="/home/team/team2.log"
APP_PORT="8088"
LOCAL_JAR_PATH="target/team-0.0.1-SNAPSHOT.jar"

echo "[1/4] Build jar"
./mvnw clean package

if [[ ! -f "${LOCAL_JAR_PATH}" ]]; then
  echo "Jar not found: ${LOCAL_JAR_PATH}"
  exit 1
fi

echo "[2/4] Upload jar to server"
scp "${LOCAL_JAR_PATH}" "${REMOTE_HOST}:${REMOTE_JAR_PATH}"

echo "[3/4] Stop current service on port ${APP_PORT}"
ssh "${REMOTE_HOST}" "PID=\$(lsof -ti tcp:${APP_PORT} || true); if [[ -n \"\$PID\" ]]; then kill \$PID; sleep 2; kill -9 \$PID 2>/dev/null || true; echo \"Stopped PID \$PID\"; else echo \"No process on ${APP_PORT}\"; fi"

echo "[4/4] Start backend with nohup"
ssh "${REMOTE_HOST}" "nohup java -jar ${REMOTE_JAR_PATH} > ${REMOTE_LOG_PATH} 2>&1 &"

echo "Deploy complete."
echo "Check logs: ssh ${REMOTE_HOST} 'tail -f ${REMOTE_LOG_PATH}'"
