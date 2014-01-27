#!/usr/bin/python
import sys
import binascii
from Crypto.Cipher import AES
from Crypto import Random
from Crypto.Util import Counter

# CBC mode with AES

def main():
	cts = []
	with open('ctr.txt', 'r') as f:
		for line in f:
			cts.append(line[:-1])
	key = cts[0]
	cts = cts[1:]

	print repr(key)
	print repr(cts)

	messages = []
	messages = decrypt_circuit(key, cts)

	print repr(messages)


def decrypt_circuit(key, cts):
	msgs = []
	for ct in cts:
		msg = []
		iv = binascii.unhexlify(ct[:32])
		print repr(iv)
		#iv_hex = binascii.hexlify(iv)
		#print repr(iv_hex)
		print "length " + repr(len(ct))

		aes = AES.new(binascii.unhexlify(key))

		next_iv = iv
		for i in range(1, len(ct)/32+1):
			next_iv = increment_iv(next_iv, i-1)
			print repr(i) + " " + repr(next_iv)
			enc_iv = aes.encrypt(next_iv)

			c = binascii.unhexlify(ct[i*32:min((i+1)*32,len(ct))])
			print repr(i) + " " + repr(c)
			m = strxor(enc_iv,c)
			print repr(i) + " " + repr(m)
			
			msg.append(m)
		msgs.append(msg)	
	return msgs

def increment_iv(iv, i):
	if i == 0:
		return iv
	else:
		iv_chars = []
		for ic in iv:
			iv_chars.append(ic)
		iv_chars[-1:] = chr(ord(iv[-1:]) +1)
		return ''.join(iv_chars)
			
def strxor(a,b):
	if len(a) > len(b):
		return "".join([chr(ord(x) ^ ord(y)) for (x,y) in zip(a[:len(b)], b)])
	else:
		return "".join([chr(ord(x) ^ ord(y)) for (x,y) in zip(a, b[:len(a)])])

if __name__ == "__main__":
	import sys
	main()
