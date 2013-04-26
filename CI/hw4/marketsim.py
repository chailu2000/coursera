'''
(c) 2011, 2012 Georgia Tech Research Corporation
This source code is released under the New BSD license.  Please see
http://wiki.quantsoftware.org/index.php?title=QSTK_License
for license details.

Created on January, 23, 2013

@author: Sourabh Bajaj
@contact: sourabhbajaj@gatech.edu
@summary: Event Profiler Tutorial
'''

import sys
import csv
import pandas as pd
import numpy as np
import math
import copy
import QSTK.qstkutil.qsdateutil as du
import datetime as dt
import QSTK.qstkutil.DataAccess as da
import QSTK.qstkutil.tsutil as tsu
import QSTK.qstkstudy.EventProfiler as ep

"""
Accepts a list of symbols along with start and end date
Returns the Event Matrix which is a pandas Datamatrix
Event matrix has the following structure :
    |IBM |GOOG|XOM |MSFT| GS | JP |
(d1)|nan |nan | 1  |nan |nan | 1  |
(d2)|nan | 1  |nan |nan |nan |nan |
(d3)| 1  |nan | 1  |nan | 1  |nan |
(d4)|nan |  1 |nan | 1  |nan |nan |
...................................
...................................
Also, d1 = start date
nan = no information about any event.
1 = status bit(positively confirms the event occurence)
"""

def main() :

    args = sys.argv
    print args

    this_file_name, initial_capital, orders_file, values_file = args
    print "initial capital: " + initial_capital
    print "orders file: " + orders_file
    print "values file: " + values_file

    trades = np.loadtxt(orders_file, dtype={'names': ('year', 'month', 'day', 'symbol', 'trade', 'shares'), 'formats': ('i4', 'i2', 'i2', 'S10', 'S10', 'f4')}, delimiter=',',converters={3: lambda s:s.strip(),4: lambda s:s.strip()})
    print trades
    order_length = len(trades)
    print "length of orders " + repr(order_length)
    vec_datetime = np.frompyfunc(dt.datetime,6,1)
    trade_ts = vec_datetime(trades['year'], trades['month'], trades['day'], np.ones(order_length,dtype=np.int)*16, np.zeros(order_length,dtype=np.int), np.zeros(order_length,dtype=np.int))
    dt_end = np.amax(trade_ts)
    dt_start = np.amin(trade_ts)
    ls_symbols = list(set(trades['symbol']))
    
    print trade_ts
    print trades

    print "Start date: " + repr(dt_start)
    print "End date: " + repr(dt_end)
    print "List of symbols: " + repr(ls_symbols)
	
    ldt_timestamps = du.getNYSEdays(dt_start, dt_end, dt.timedelta(hours=16))

    dataobj = da.DataAccess('Yahoo')
    ls_keys = ['open', 'high', 'low', 'close', 'volume', 'actual_close']
    ldf_data = dataobj.get_data(ldt_timestamps, ls_symbols, ls_keys)
    d_data = dict(zip(ls_keys, ldf_data))
    for s_key in ls_keys:
        d_data[s_key] = d_data[s_key].fillna(method = 'ffill')
        d_data[s_key] = d_data[s_key].fillna(method = 'bfill')
        d_data[s_key] = d_data[s_key].fillna(1.0)

    print d_data['close']

    raw_cash = np.ones(len(ldt_timestamps), dtype=float)*int(initial_capital)
    cash = pd.DataFrame(raw_cash,index=ldt_timestamps,columns=['cash'])
    print "cash before update: " 
    print cash
    cash = updateCash(cash, trades, trade_ts, d_data['close'])
    print "cash after update: " 
    print cash

    ownership = getOwnership(trades, trade_ts, ldt_timestamps, d_data['close'])
    print ownership

    equity = getEquityValue(ownership, d_data['close'])
    print equity

    print "\nTotal value: " 

    total = cash['cash'] + equity['equity']
    print total

    #new_index = np.empty((len(total.index),3),dtype=np.int)
    #for i in range(len(total.index)):
#	date = total.index[i]
#	new_index[i] = [date.year, date.month, date.day]
    #total.index = pd.MultiIndex.from_tuples(new_index, names=['None','2','3'])
    #total.to_csv(values_file, ', ')

    with open(values_file, 'w') as of:
	for i in range(len(total.index)):
	    date = total.index[i]
	    value = total.ix[date]
	    writer = csv.writer(of)
	    writer.writerow([date.year,date.month,date.day,value])

def updateCash(c, t, ts_trade, p) :
    print ts_trade
    for i in range(len(t)):
	price = p.get_value(ts_trade[i],t['symbol'][i])
	amt_trade = t['shares'][i] * price
	if t['trade'][i].lower() == 'buy': 
	    amt_trade = -1 * amt_trade
	for j in c.index:
	    if (j - ts_trade[i]) >= dt.timedelta(seconds = 0):
		c.ix[j] = c.ix[j] + amt_trade

    return c

def getOwnership(t, ts_trade, ts, p) :
    os = np.zeros(np.shape(p), dtype=float)
    ownership = pd.DataFrame(os,index=ts,columns=p.columns)
    for i in range(len(t)):
	shares = t['shares'][i]
	symbol = t['symbol'][i]
	if t['trade'][i].lower() == 'sell':
	    shares = -1 * shares 
	for j in ownership.index:
	    if (j - ts_trade[i]) >= dt.timedelta(seconds = 0):
		ownership.ix[j][symbol] = ownership.ix[j][symbol] + shares
    return ownership

def getEquityValue(o, p):
    eq = np.empty(len(o), dtype=float)
    equity = pd.DataFrame(eq,index=p.index,columns=['equity'])
    for date,row in o.iterrows():
	equity.ix[date]['equity'] = np.dot(row, p.ix[date])

    return equity

if __name__ == '__main__':
	main()
