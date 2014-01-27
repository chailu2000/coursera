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
import sys
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
    args = sys.argv
    print args

    this_file_name, values_file, benchmark_symbol = args
    print "values file: " + values_file
    print "benchmark symbol: " + benchmark_symbol

    values = np.loadtxt(values_file, dtype={'names': ('year','month','day','value'), 'formats': ('i4','i2','i2','f4')}, delimiter=',')
    print values

    value_length = len(values)
    print "value length: " + repr(value_length)

    vec_datetime = np.frompyfunc(dt.datetime,6,1)
    values_ts = vec_datetime(values['year'], values['month'], values['day'], np.ones(value_length,dtype=np.int)*16, np.zeros(value_length,dtype=np.int), np.zeros(value_length,dtype=np.int))
    dt_end = np.amax(values_ts)
    dt_start = np.amin(values_ts)

    print values_ts

    ldt_timestamps = du.getNYSEdays(dt_start, dt_end, dt.timedelta(hours=16))

    ls_symbols = [benchmark_symbol]

    raw_data,trading_days = loadData(dt_start, dt_end, ls_symbols)
    #print raw_data
    print "Number of trading days: " + repr(trading_days)
    b_vol, b_daily_ret, b_sharpe, b_cum_ret = simulate(raw_data, trading_days)

    pv = pd.DataFrame(values['value'],index=ldt_timestamps,columns=['value'])
    port_values = pv.values
 
    #print port_values
    vol, daily_ret, sharpe, cum_ret = simulate(port_values, trading_days)

    print "Start date: " + repr(dt_start)
    print "End date: " + repr(dt_end)
    print "Sharpe ratio of fund: " + repr(sharpe)
    print "Sharpe ratio of $SPX: " + repr(b_sharpe)
    print "Total Return of fund: " + repr(cum_ret)
    print "Total Return of $SPX: " + repr(b_cum_ret)
    print "Standard deviation of fund: " + repr(vol)
    print "Standard deviation of $SPX: " + repr(b_vol)
    print "Average Daily Return of fund: " + repr(daily_ret)
    print "Average Daily Return of $SPX: " + repr(b_daily_ret)

    # Plotting the plot of daily returns
    raw_data = port_values[0][0] * raw_data / raw_data[0, :]
    plt.clf()
    plt.plot(ldt_timestamps, raw_data)  # $SPX during the period
    plt.plot(ldt_timestamps, port_values)  # Fund during the period
    plt.axhline(y=0, color='r')
    plt.legend(['$SPX', 'Fund'])
    plt.ylabel('Fund Value')
    plt.xlabel('Date')
    plt.savefig('Total-Value.pdf', format='pdf')

def simulate(na_price, trading_days):

    # Normalizing the prices to start at 1 and see relative returns
    na_normalized_price = na_price / na_price[0, :]

    # Copy the normalized prices to a new ndarry to find returns.
    na_rets = na_normalized_price.copy()

    # portifolio price
    port_rets = na_rets

    port_price = port_rets.copy()

    # Calculate the daily returns of the prices. (Inplace calculation)
    # returnize0 works on ndarray and not dataframes.
    tsu.returnize0(port_rets)

    #print port_rets

    # portifolio cumulative return
    port_cum = port_price[-1]

    # standard deviation
    vol = np.std(port_rets)
    daily_ret = np.mean(port_rets)
    sharpe = np.sqrt(252) * daily_ret / vol
    #sharpe = np.sqrt(trading_days) * daily_ret / vol
    cum_ret = port_cum[0]

    return vol,daily_ret,sharpe,cum_ret


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
