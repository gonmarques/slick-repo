#!/bin/bash
su - db2inst1 <<'EOF'
db2start
db2 create db SLICK
while true; do sleep 1000; done
EOF
