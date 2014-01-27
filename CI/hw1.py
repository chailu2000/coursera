'''
(c) 2011, 2012 Georgia Tech Research Corporation
This source code is released under the New BSD license.  Please see
http://wiki.quantsoftware.org/index.php?title=QSTK_License
for license details.

Created on January, 24, 2013

@author: Sourabh Bajaj
@contact: sourabhbajaj@gatech.edu
@summary: Example tutorial code.
'''

# QSTK Imports
import QSTK.qstkutil.qsdateutil as du
import QSTK.qstkutil.tsutil as tsu
import QSTK.qstkutil.DataAccess as da

# Third Party Imports
import datetime as dt
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np

print "Pandas Version", pd.__version__


def main():
    ''' Main Function'''
    # List of symbols
    #ls_symbols = ['AAPL','GLD','GOOG','XOM']
    ls_symbols = ['BRCM','TXN','AMD','ADI']
    #ls_symbols = ['GOOG','AAPL','GLD','XOM']
    #ls_symbols = ['AXP','HPQ','IBM','HNZ']

    dt_start = dt.datetime(2011, 1, 1)
    dt_end = dt.datetime(2011, 12, 31)

    max_sharpe = [0.0,0.0,0.0,0.0]
    opt_alloc = []
    raw_data,trading_days = loadData(dt_start, dt_end, ls_symbols)
    for i in [0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0]:
    	for j in [0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0]:
    	    for k in [0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0]:
    	        for l in [0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0]:
			if i+j+k+l == 1.0:
    			    vol, daily_ret, sharpe, cum_ret = simulate(raw_data, [i,j,k,l],trading_days)
			    print "Allocation: " 
			    print [i,j,k,l] 
			    print "Standard deviation: " 
			    print vol
			    print "Mean Daily Return: " 
			    print daily_ret
			    print "Sharpe ratio: " 
			    print sharpe
			    print "Cumulative Daily Return: " 
			    print cum_ret
			    if max_sharpe[2] < sharpe :
				max_sharpe = [vol,daily_ret,sharpe,cum_ret]
				opt_alloc = [i,j,k,l]
    print "Start date: " 
    print dt_start
    print "End date: " 
    print dt_end
    print "Portifolio: " 
    print ls_symbols
    print "Optimized Allocation: " 
    print opt_alloc
    print "Standard deviation: " 
    print max_sharpe[0]
    print "Mean Daily Return: " 
    print max_sharpe[1]
    print "Sharpe ratio: " 
    print max_sharpe[2]
    print "Cumulative Daily Return: " 
    print max_sharpe[3]

def simulate(na_price, allocations, trading_days):

    # Normalizing the prices to start at 1 and see relative returns
    na_normalized_price = na_price / na_price[0, :]

    # Copy the normalized prices to a new ndarry to find returns.
    na_rets = na_normalized_price.copy()

    # portifolio price
    port_rets = np.sum(na_rets * allocations, axis=1)

    port_price = port_rets.copy()

    # Calculate the daily returns of the prices. (Inplace calculation)
    # returnize0 works on ndarray and not dataframes.
    tsu.returnize0(port_rets)

    #print port_rets

    # portifolio cumulative return
    port_cum = port_price[-1] - 1

    # standard deviation
    vol = np.std(port_rets)
    daily_ret = np.mean(port_rets)
    sharpe = np.sqrt(trading_days) * daily_ret / vol
    cum_ret = port_cum

    return vol,daily_ret,sharpe,cum_ret

    # Plotting the scatter plot of daily returns between XOM VS $SPX
    #plt.clf()
    #plt.scatter(na_rets[:, 3], na_rets[:, 4], c='blue')
    #plt.ylabel('XOM')
    #plt.xlabel('$SPX')
    #plt.savefig('scatterSPXvXOM.pdf', format='pdf')

    # Plotting the scatter plot of daily returns between $SPX VS GLD
    #plt.clf()
    #plt.scatter(na_rets[:, 3], na_rets[:, 1], c='blue')  # $SPX v GLD
    #plt.ylabel('GLD')
    #plt.xlabel('$SPX')
    #plt.savefig('scatterSPXvGLD.pdf', format='pdf')

def loadData(dt_start, dt_end, portfolio_symbols):
    # We need closing prices so the timestamp should be hours=16.
    dt_timeofday = dt.timedelta(hours=16)

    # Get a list of trading days between the start and the end.
    ldt_timestamps = du.getNYSEdays(dt_start, dt_end, dt_timeofday)

    # Creating an object of the dataaccess class with Yahoo as the source.
    c_dataobj = da.DataAccess('Yahoo', cachestalltime=0)

    # Keys to be read from the data, it is good to read everything in one go.
    ls_keys = ['open', 'high', 'low', 'close', 'volume', 'actual_close']

    # Reading the data, now d_data is a dictionary with the keys above.
    # Timestamps and symbols are the ones that were specified before.
    ldf_data = c_dataobj.get_data(ldt_timestamps, portfolio_symbols, ls_keys)
    d_data = dict(zip(ls_keys, ldf_data))

    # Getting the numpy ndarray of close prices.
    na_price = d_data['close'].values

    return na_price,len(ldt_timestamps)

if __name__ == '__main__':
    main()
