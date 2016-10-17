#!/bin/bash
su - db2inst1 <<'EOF'
db2start
db2 create db SLICK
EOF
