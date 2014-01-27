#!/usr/bin/python

import gmpy2
from gmpy2 import mpz
import sys
from datetime import datetime


def main():
	start = datetime.now()
	print start

	pgh = []
	with open('pgh.txt', 'r') as f:
		for line in f:
			pgh.append(line[:-2])
	print pgh

	x = dlog(pgh)

	print x

	end = datetime.now()
	print end

	delta = end - start
	print delta

def dlog(pgh):

	p = pgh[0]
	g = pgh[1]
	h = pgh[2]

	B = gmpy2.powmod(2,20,mpz(p))
	B_low = 0
	#B_low = gmpy2.powmod(2,18,mpz(p))
	print "B: " + repr(B)

	# build the hash
	hash_table = {}
	for i in range(B_low,B):
		left = gmpy2.divm(mpz(h),gmpy2.powmod(mpz(g),i, mpz(p)),mpz(p))
		try:
			hash_table[left]
			print "collision at " + i
		except KeyError:
			hash_table[left] = i
			continue

	print "Hash table built"
	#print "keys: " + repr(hash_table.keys())

	counter = mpz(0)
	# meet in the middle
	x0 = mpz(0)
	x1 = mpz(0)
	for i in range(B_low,B):
		counter = gmpy2.add(counter,mpz(1))
		right = gmpy2.powmod(gmpy2.powmod(mpz(g),mpz(B),mpz(p)),i,mpz(p))
		try:
			h_entry = hash_table[right] 
			print 'found x0 and x1...'
			x0 = i
			x1 = h_entry
			print 'x0=' + repr(x0)
			print 'x1=' + repr(x1)
			
			break
		except KeyError:
			continue

	print "counter: " + repr(counter)
	x = gmpy2.t_mod(gmpy2.add(gmpy2.mul(x0,B),x1), mpz(p))
	return x

if __name__ == "__main__":
	import sys
	main()
