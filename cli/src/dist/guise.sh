#!/usr/bin/env bash
# Guise CLI Launcher 0.1.0
# Copyright (c) 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>

set -o errexit
set -o pipefail
set -o nounset

EXE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
java -jar ${EXE_DIR}/guise-cli-0.1.0-exe.jar "$@"
