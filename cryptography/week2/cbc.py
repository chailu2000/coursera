#!/usr/bin/python
import sys
import binascii
from Crypto.Cipher import AES
from Crypto import Random

# CBC mode with AES

def main():
	cts = []
	with open('cbc.txt', 'r') as f:
		for line in f:
			cts.append(line[:-2])
	key = cts[0]
	cts = cts[1:]

	print repr(key)
	print repr(cts)

	messages = []
	messages = decrypt_circuit(key, cts)

	print repr(messages)


def decrypt_circuit(key, cts):

	aes = AES.new(binascii.unhexlify(key))

	msgs = []
	for ct in cts:
		msg = []
		iv = binascii.unhexlify(ct[:32])
		print repr(iv)
		#iv_hex = binascii.hexlify(iv)
		#print repr(iv_hex)
		print "length " + repr(len(ct))
		for i in range(1, len(ct)/32):
			c_left = binascii.unhexlify(ct[(i-1)*32:i*32])
			print repr(i) + " " + repr(c_left)
			c_right = binascii.unhexlify(ct[i*32:(i+1)*32])
			print repr(i) + " " + repr(c_right)
			m = strxor(c_left,aes.decrypt(c_right))
			print repr(i) + " " + repr(m)
			
			if (i+1)*32 == len(ct) :
				m = drop_padding(m)

			msg.append(m)
		msgs.append(msg)	
	return msgs
			
def drop_padding(m):
	last_bit = m[-1:]
	print "last_bit " + repr(last_bit)
	n = int('0' + repr(last_bit)[2:-1], 16)
	print "padding length " + repr(n)
	print "m before " + repr(m)
	m = m[:-(n)]
	print "m after " + repr(m)
	return m

def strxor(a,b):
	if len(a) > len(b):
		return "".join([chr(ord(x) ^ ord(y)) for (x,y) in zip(a[:len(b)], b)])
	else:
		return "".join([chr(ord(x) ^ ord(y)) for (x,y) in zip(a, b[:len(a)])])

if __name__ == "__main__":
	import sys
	main()
