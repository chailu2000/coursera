'''
Created on Apr 13, 2013

@author: ubuntu
'''
import pandas as pd
import numpy as np
import math
import copy
import QSTK.qstkutil.qsdateutil as du
import datetime as dt
import QSTK.qstkutil.DataAccess as da
import QSTK.qstkutil.tsutil as tsu
import QSTK.qstkstudy.EventProfiler as ep
import matplotlib.pyplot as plt

def main():
    
    dt_start = dt.datetime(2009, 12, 1)
    dt_end = dt.datetime(2010, 12, 31)
    dataobj = da.DataAccess('Yahoo')
    ls_symbols = ['MSFT']
    # We need closing prices so the timestamp should be hours=16.
    dt_timeofday = dt.timedelta(hours=16)
    # Get a list of trading days between the start and the end.
    ldt_timestamps = du.getNYSEdays(dt_start, dt_end, dt_timeofday)

    na_price,trading_days = loadData(ldt_timestamps, ls_symbols)

    dt_start_real = dt.datetime(2010, 1, 1)

    rmean = pd.rolling_mean(na_price, 20)
    
    rstd = pd.rolling_std(na_price, 20)
    
    upper = rmean + rstd
    
    lower = rmean - rstd
    
    Bollinger_val = (na_price - rmean) / (rstd)

    bollinger_vals = pd.DataFrame(Bollinger_val,index=ldt_timestamps,columns=['bollinger'])
    for i in range(len(bollinger_vals.index)):
        date = bollinger_vals.index[i]
        value = bollinger_vals.ix[date]
        print repr(date) + " " + repr(value)
    # Plotting the plot of daily returns
    plt.clf()
    plt.plot(ldt_timestamps, Bollinger_val)  # Bollinger_val during the period
    plt.axhline(y=0, color='r')
    plt.legend(['GOOG Bollinger'],'upper left')
    plt.ylabel('Stock Value')
    plt.xlabel('Date')
    #plt.vlines(ldt_timestamps, 0, 700, color='gray', linestyles='solid')
    plt.savefig('bollinger-value.pdf', format='pdf')
    
    
    # Plotting the plot of daily returns
    plt.clf()
    plt.plot(ldt_timestamps, na_price)  # price during the period
    plt.plot(ldt_timestamps, rmean)  # rolling mean during the period
    plt.plot(ldt_timestamps, upper)  # upper band during the period
    plt.plot(ldt_timestamps, lower)  # lower band during the period
    plt.axhline(y=0, color='r')
    plt.legend(['GOOG','SMA','Bollinger Upper Band','Bollinger Lower Band'],'lower right')
    plt.ylabel('Stock Value')
    plt.xlabel('Date')
    #plt.vlines(ldt_timestamps, 0, 700, color='gray', linestyles='solid')
    plt.savefig('bollinger.pdf', format='pdf')

def loadData(ldt_timestamps, portfolio_symbols):

    # Creating an object of the dataaccess class with Yahoo as the source.
    c_dataobj = da.DataAccess('Yahoo', cachestalltime=0)

    # Keys to be read from the data, it is good to read everything in one go.
    ls_keys = ['open', 'high', 'low', 'close', 'volume', 'actual_close']

    # Reading the data, now d_data is a dictionary with the keys above.
    # Timestamps and symbols are the ones that were specified before.
    ldf_data = c_dataobj.get_data(ldt_timestamps, portfolio_symbols, ls_keys)
    d_data = dict(zip(ls_keys, ldf_data))
    for s_key in ls_keys:
        d_data[s_key] = d_data[s_key].fillna(method = 'ffill')
        d_data[s_key] = d_data[s_key].fillna(method = 'bfill')
        d_data[s_key] = d_data[s_key].fillna(1.0)

    # Getting the numpy ndarray of close prices.
    na_price = d_data['close'].values

    return na_price,len(ldt_timestamps)


if __name__ == '__main__':
    main()