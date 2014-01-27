#!/usr/bin/python
import sys
import os
from Crypto.Hash import SHA256

# SHA256 hash on every 1k chunk of a large file

def main():
	file_name = 'v1.mp4'
	block_size = 1024
	statinfo = os.stat(file_name)
	file_size = statinfo.st_size
	print 'File name: ' + file_name
	print 'File size: ' + repr(file_size)
	last_block_size =  file_size % block_size
	with open(file_name, 'rb') as f:
		block = bytearray()
		hash_block = bytearray()
		if last_block_size != 0:
			pos = (-1)*last_block_size
			f.seek(pos, 2) # offset relative to the end
			block = f.read(last_block_size) # read to the end
			h = SHA256.new(data=block)
			hash_block = h.digest()
			last_block_size = 0
			print 'last block hash: ' + h.hexdigest() + ' pos: ' + repr(f.tell())
			
		for i in range(file_size/block_size,0,-1):
			pos = pos - block_size
			f.seek(pos, 2)
			block = f.read(block_size)
			block = block + hash_block
			h = SHA256.new(data=block)
			hash_block = h.digest()
			print 'block ' + repr(i) + ' hash: ' + h.hexdigest() + ' pos: ' + repr(f.tell())

	print 'Hash: ' + repr(h.hexdigest())

if __name__ == "__main__":
	import sys
	main()
