all:
	gcc -shared -O2 -o ddb_remote.so  remote.c  -fPIC -march=native