#!/bin/bash

LIB=ddb_remote.so
make && cat ${LIB} > ~/.local/lib/deadbeef/${LIB} && echo "Copy ${LIB} to ~/.local/lib/deadbeef/" && rm ${LIB} && echo "Done!"