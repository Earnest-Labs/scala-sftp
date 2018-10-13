#!/bin/bash

set -e
export SFTP_USER="foo"
export SFTP_PASSWORD="pass"
export SFTP_HOST="sftp"
export SFTP_PORT="22"
export SFTP_STRICT_HOST_KEY_CHECKING="no"

set -vx
exec "$@"
echo "done"
