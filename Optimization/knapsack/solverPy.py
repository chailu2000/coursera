#!/usr/bin/python
# -*- coding: utf-8 -*-


def solveIt(inputData):
    # Modify this code to run your optimization algorithm

    # parse the input
    lines = inputData.split('\n')

    firstLine = lines[0].split()
    items = int(firstLine[0])
    capacity = int(firstLine[1])

    values = []
    weights = []

    taken = []

    for i in range(1, items+1):
        line = lines[i]
        parts = line.split()

        values.append(int(parts[0]))
        weights.append(int(parts[1]))
        # initialize taken
        taken.append(0)

    items = len(values)

    # Take items in high to low order 
    # ranked by v/w to fill the knapsack
    # until the knapsack is full
    value = 0
    weight = 0
    vws = []

    # initialize v/w ratio with index list
    for i in range(0, items):
        vws.append([float(values[i])/float(weights[i]), values[i], weights[i], i])
    
    #print vws    
    # sort the list from high to low on the v/w ration, and low to high on weight
    vws = sorted(vws, key=lambda x: -float(x[0]) or float(x[2]))
    #print vws
    # take from vws until sack is full
    for i in range(0, items):
        if weight + vws[i][2] <= capacity:
            value += vws[i][1]
            weight += vws[i][2]
            taken[vws[i][3]] = 1
    
    print capacity, weight
    # a trivial greedy algorithm for filling the knapsack
    # it takes items in-order until the knapsack is full
    #value = 0
    #weight = 0
    #taken = []

    #for i in range(0, items):
    #    if weight + weights[i] <= capacity:
    #        taken.append(1)
    #        value += values[i]
    #        weight += weights[i]
    #    else:
    #        taken.append(0)

    # prepare the solution in the specified output format
    outputData = str(value) + ' ' + str(0) + '\n'
    outputData += ' '.join(map(str, taken))
    return outputData


import sys

if __name__ == '__main__':
    if len(sys.argv) > 1:
        fileLocation = sys.argv[1].strip()
        inputDataFile = open(fileLocation, 'r')
        inputData = ''.join(inputDataFile.readlines())
        inputDataFile.close()
        print solveIt(inputData)
    else:
        print 'This test requires an input file.  Please select one from the data directory. (i.e. python solver.py ./data/ks_4_0)'

