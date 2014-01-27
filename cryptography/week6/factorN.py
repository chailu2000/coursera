#!/usr/bin/python

import gmpy2
from gmpy2 import mpz
import sys
from datetime import datetime


def main():
	start = datetime.now()
	print start

	Ns = []
	with open('Ns.txt', 'r') as f:
		for line in f:
			Ns.append(line[:-2])
	print Ns

	ps = []
	for i in range(len(Ns)-2):
		print "factoring " + repr(Ns[i])
		ps.append(factor(Ns[i]))

	with open('Ps.txt', 'w') as f:
		for i in range(len(ps)):
			f.write(repr(ps[i]))
			f.write('\n')

	print "factoring " + repr(Ns[2])
	factor3(Ns[2])

	reverseRSA(mpz(Ns[3]),mpz(Ns[0]),mpz(ps[0]),gmpy2.c_div(mpz(Ns[0]),mpz(ps[0])))

	end = datetime.now()
	print end

	delta = end - start
	print delta

def reverseRSA(ct,N,p,q):
	assert gmpy2.mul(p,q) == mpz(N)
	phi = gmpy2.sub(N,p)
	phi = gmpy2.sub(phi,q)
	phi = gmpy2.add(phi,1)
	d = gmpy2.invert(mpz(65537),phi)
	print "d: " + repr(d)
	print "ct: " + repr(ct)
	
	m = gmpy2.powmod(ct,d,N)
	print repr(m.digits(16))
	
def factor3a(N):
	n = gmpy2.mul(mpz(N),6)
	p = factor(n)
	p = gmpy2.c_div(p,3)

def factor3(N):
	n = mpz(N)
	cnt = 0
	A0 = gmpy2.isqrt(gmpy2.mul(n,6))
	print "A0: " + repr(A0)
	for i in range(1):
		cnt = cnt + 1
		A = gmpy2.add(A0, 1)
		#print "A: " + repr(A)
		x = gmpy2.sub(gmpy2.mul(A,A),gmpy2.mul(6,n))
		x = gmpy2.mul(4,gmpy2.sub(x,A))
		x = gmpy2.add(1,x)
		x = gmpy2.isqrt(x)
		x = gmpy2.sub(x,1)
		x = gmpy2.c_div(x,2)
		#print "x: " + repr(x)
		p = gmpy2.c_div(gmpy2.sub(gmpy2.sub(A,x),1),3)
		#print "p: " + repr(p)
		q = gmpy2.c_div(gmpy2.add(A,x),2)
		#print "q: " + repr(q)
		# verify that pq = N
		if (gmpy2.mul(p,q) == n):
			print "A: " + repr(A)
			print "p: " + repr(p)
			print "counter: " + repr(cnt)
			break
	else:
		print repr(gmpy2.sub(n,gmpy2.mul(p,q)))
		print "Was not able to factor " + repr(N)

	
def factor(N):
	n = mpz(N)
	cnt = 0
	A0 = gmpy2.isqrt(n) + 1
	for i in range(gmpy2.powmod(2,20,n)):
		cnt = cnt + 1
		A = gmpy2.add(A0,i)

		x = gmpy2.isqrt(gmpy2.sub(gmpy2.mul(A,A), n))

		p = gmpy2.sub(A , x)

		# verify that pq = N
		if (gmpy2.mul(p,(gmpy2.add(A,x))) == n):
			print "A: " + repr(A)
			print "x: " + repr(x)
			print "p: " + repr(p)
			print "counter: " + repr(cnt)
			break
	else:
		print "Was not able to factor " + repr(N)

	return p

if __name__ == "__main__":
	import sys
	main()
