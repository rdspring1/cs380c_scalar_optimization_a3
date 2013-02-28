all: hw3
	tar -zcvf hw3.tar.gz hw3
	turnin --submit olivo cs380c-hw3 hw3.tar.gz
	turnin --list olivo cs380c-hw3
