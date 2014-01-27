import urllib2
import sys
from struct import *
import array
from time import sleep
import datetime

TARGET = 'http://crypto-class.appspot.com/po?er='
#--------------------------------------------------------------
# padding oracle
#--------------------------------------------------------------
class PaddingOracle(object):
    def query(self, q):
        target = TARGET + urllib2.quote(q)    # Create query URL
        req = urllib2.Request(target)         # Send HTTP request to server
        try:
            f = urllib2.urlopen(req)          # Wait for response
        except urllib2.HTTPError, e:          
            print "We got: %d" % e.code       # Print response code
            if e.code == 404:
                return True # good padding
            return False # bad padding
	print "We got: 200"
	return True

    def guess(self):
	with open('po_cipher.txt', 'r') as f:
		for line in f:
			ct = line
	#print ct
	
	cts = []
	for i in range(len(ct)/32):
		cts.append(ct[i*32:(i+1)*32])
	print cts

	asciis = []
	asciis.extend([x for x in range(16,1,-1)])
	asciis.extend(x for x in [101,116,97,111,105,110,115,32,104,114,100,108,99,117,109,119,102,103,121,112,98,118,107,106,120,113,122])
	asciis.extend([x for x in range(33,97)])
	asciis.extend([x for x in range(123,127)])
	asciis.append(1)
	print asciis

	print asciis[15:-1]

	loop_count = 0
	pts = []
	#for j in range(0,len(cts)-1):
	#for j in range(len(cts)-4,len(cts)-3):
	for j in range(len(cts)-2,-1,-1):
		m_bytes = []
		wcts = list(cts)
		if j == len(cts)-1:
			asciis = asciis[15:-1]
		for k in range(16):
			for n in range(len(asciis)):
				loop_count = loop_count + 1
				l = asciis[n]
				a = cts[j] 
				b = get_padded_guess(l,k)
				print a # cipher text
				print b # guess
				if len(m_bytes) > 0:
					m = ''.join(x for x in m_bytes)
					b1 = hexor(b,m)
					print b1 # guess with lower bytes
					b = b1
				g = hexor(a,b) 
				print g 
				c = getpad(k+1) # pad
				g = hexor(g, c)
				print c
				print g
				wcts[j] = g
				q = ''.join(x for x in wcts[:j+2])
				print q # query cipher text
				sleep(1)	
				response = self.query(q)
				if response == True:
					m_bytes.insert(0, pack('B',l).encode('hex'))
					break
			else:
				if n == len(asciis)-1:
					print "did not find a good byte..."
					print "previous guesses: " + m_bytes
					sys.exit(0)
				continue
		else:
			pts.insert(0, ''.join(x for x in m_bytes))
			print pts
	print ''.join(x.rstrip('\\x') for x in pts).decode('hex')
	print 'loop count: ' + repr(loop_count)
def get_padded_guess(ii,jj):
	# ii - guess
	# jj - byte index from end
	h = pack('B',ii).encode('hex')
	#print h
	z = '00000000000000000000000000000000'
	if ii == 0:
		return z
	if jj == 0:
		h = z[:(-2)*(jj+1)] + h
	elif jj == 15:
		h = h + z[(-2)*jj:]
	else:	
		h = z[:(-2)*(jj+1)] + h + z[(-2)*jj:]
	#print h
	return h
def getpad(ind):
	inds = []
	fmt = ''
	for i in range(ind):
		inds.append(ind)
		fmt = fmt + 'b'
	pad = pack(fmt, *inds)
	pad = pad.encode('hex')
	p = pad
	for i in range(32-len(pad)):
		p = '0' + p
	return p
	
def hexor(hex1, hex2):
	xor = hex(int(hex1,16) ^ int(hex2,16))
	return xor[2:].rstrip("L")

		
if __name__ == "__main__":
	start = datetime.datetime.now()
	print start

    	po = PaddingOracle()

	po.guess()

	end = datetime.datetime.now()

	print end
	delta = end - start
	print delta
