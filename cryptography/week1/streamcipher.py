#!/usr/bin/python
import sys
import binascii

def strxor(a,b):
	if len(a) > len(b):
		return "".join([chr(ord(x) ^ ord(y)) for (x,y) in zip(a[:len(b)], b)])
	else:
		return "".join([chr(ord(x) ^ ord(y)) for (x,y) in zip(a, b[:len(a)])])

def main():
	ciphers = []
	with open('ciphers.txt', 'r') as f:
		for line in f:
			ciphers.append(binascii.unhexlify(line[:-2]))
#	print ciphers

#	cipher_xor = reduce(strxor, ciphers)
#	print cipher_xor.encode('hex')
	with open('m_xor_mt.txt', 'w') as f:
		for i in range(10):
			f.write(repr(i) + ' - mt ')
			f.write(repr(strxor(ciphers[i], ciphers[10]).encode('hex')))
			f.write('\n')
	for i in range(10):
		with open('m_xor_m' + repr(i) + '.txt', 'w') as f:
			for j in range(10):
				if (i == j): 
					continue
				f.write(repr(j) + ' - ' + repr(i) + ' ')
				f.write(repr(ppnt(strxor(ciphers[j], ciphers[i]))))
				f.write('\n')
#	guess_key(ciphers)

def ppnt(ciphertext):
	ct = []
	for x in ciphertext:
		if ord(x) in range(0x41, 0x5a) or ord(x) in range(0x61, 0x7a):
			ct.append(x)
		else:
			ct.append(' ')
	return ct
#	print strxor(ciphers[7], ciphers[10]).encode('hex')
#	print strxor(ciphers[8], ciphers[10]).encode('hex')
#	print strxor(ciphers[9], ciphers[10]).encode('hex')
	
def guess_key(ciphers):
	key_space = range(0x20, 0x7f)
	msg_space = range(0x20, 0x7f)
	minlength = min_length(ciphers)


	transposed_ciphers = transposeCiphers(ciphers, minlength)
	key = []
	letters = []
	for ci in transposed_ciphers:
		key_pos = []
		for k in key_space:
			key_found = 0
			#print k, ci
			for x in ci:
				if k ^ ord(x) in msg_space:
					key_found = key_found + 1
			if key_found == len(ci):
				#print repr(k) + " good for " + repr(ci)
				key_pos.append(k)
		key.append(key_pos)

		ci_pos = letterPosition(ci)
		letters.append(ci_pos)

	multi = 1
	for i in range(len(key)):
		if len(key[i]) ==0:
			print i
		else:
			multi = multi * len(key[i])

	print 'number of possible keys: ' + repr(multi)

	for i in range(len(letters)):
		print i, repr(letters[i])

	with open('transposed.txt', 'w') as f:
		for ci in transposed_ciphers:
			f.write(repr(ci))
			f.write('\n')
	with open('letter_pos.txt', 'w') as f:
		for i in range(len(letters)):
			f.write(repr(i))
			f.write(repr(letters[i]))
			f.write('\n')
	with open('possible_keys.txt', 'w') as f:
		for k in key:
			f.write(repr(k))
			f.write('\n')

		

def letterPosition(ci):
	letter_pos = []
	for i in range(len(ci)):
		x = ord(ci[i])
		if x in range(0x41,0x5a) or x in range(0x61,0x7a) :
			letter_pos.append(i)
	return letter_pos

def min_length(ciphers):
	minlength = 10000
	for ci in ciphers:
		minlength = min(len(ci), minlength)
	return minlength

def transposeCiphers(ciphers, length):
	tc = []
	for i in range(length):
		tcrow = []
		for ci in ciphers:
			tcrow.append(ci[i])
		tc.append(tcrow)
	return tc
	
if __name__ == "__main__":
	import sys
	main()
