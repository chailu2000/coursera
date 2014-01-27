#!/usr/bin/env python
import mincemeat

from os import listdir
from os.path import isfile, join

datafiles = [ f for f in listdir("hw3data") if isfile(join("hw3data",f)) ]

# The data source can be any dictionary-like object
datasource = dict(enumerate(datafiles))
print datasource


def mapfn(k, v):
    import stopwords
    import re
    # read file line by line
    with open('hw3data/'+v, 'r') as f:
        print f
        for line in f:
            fields = line.split(":::")
            authors = fields[1].split("::")
            #print authors
            terms = fields[2].split()
            #print terms
            for a in authors:
                for t in terms:
                    if (stopwords.allStopWords.has_key(t.lower()) == False and len(t) > 1):
                        t = t.replace("-", " ")
                        yield a,re.sub('[^a-zA-Z0-9_]','',t)

def reducefn(k, vs):
    t_fs = {}

    for v in vs:
        if (t_fs.has_key(v) == False):
            t_fs[v] = 1
        else:
            t_fs[v] = t_fs[v] + 1 

    return t_fs

s = mincemeat.Server()
s.datasource = datasource
s.mapfn = mapfn
s.reducefn = reducefn

results = s.run_server(password="changeme")
with open('results.txt', 'w') as f:
    for k in results.keys():
        f.write(k + " => " + repr(results[k]) + "\n")
