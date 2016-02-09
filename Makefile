all:
	gcc -I/usr/include -I/opt/deadbeef/include -shared -O2 -o ddb_remote.so  remote.c  -fPIC -march=native