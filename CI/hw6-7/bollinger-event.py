'''
Created on Apr 13, 2013

@author: ubuntu
'''
import pandas as pd
import numpy as np
import math
import copy
import csv
import QSTK.qstkutil.qsdateutil as du
import datetime as dt
import QSTK.qstkutil.DataAccess as da
import QSTK.qstkutil.tsutil as tsu
import QSTK.qstkstudy.EventProfiler as ep
import matplotlib.pyplot as plt

def main():
    
    dt_start = dt.datetime(2008, 1, 1)
    dt_end = dt.datetime(2009, 12, 31)
    # We need closing prices so the timestamp should be hours=16.
    dt_timeofday = dt.timedelta(hours=16)
    # Get a list of trading days between the start and the end.
    ldt_timestamps = du.getNYSEdays(dt_start, dt_end, dt_timeofday)

    print "Getting price data... " 

    ls_symbols, d_data, d_close,trading_days = loadData(ldt_timestamps)

    print "Getting bollinger data... " 

    bollinger = find_bollinger(d_close, ls_symbols, ldt_timestamps)
    print bollinger
   
    print "Finding bollinger events... "
    df_events,cnt = find_events(ls_symbols, bollinger)
    print "Num. of events: " 
    print cnt

    print "Creating Study"
    ep.eventprofiler(df_events, d_data, i_lookback=20, i_lookforward=20,
        s_filename='MyEventStudy.pdf', b_market_neutral=True, b_errorbars=True,
        s_market_sym='SPY')  
      
def find_events(ls_symbols, bollinger):
    print "Finding Events"

    # Time stamps for the event range
    ldt_timestamps = bollinger.index
    
    df_events = copy.deepcopy(bollinger)
    df_events = df_events * np.NAN

    cnt = 0
    fileName = "orders.csv" 

    with open(fileName, 'w') as of: 
        for i in range(1, len(ldt_timestamps)):
            f_spybollinger_today = bollinger['SPY'].ix[ldt_timestamps[i]]
            for s_sym in ls_symbols:
                # Find the price for this timestamp
                f_symbollinger_today = bollinger[s_sym].ix[ldt_timestamps[i]]
                f_symbollinger_yest = bollinger[s_sym].ix[ldt_timestamps[i - 1]]
        
                # Event is found if the symbol is down below 5
                if f_symbollinger_today <= -2.0 and f_symbollinger_yest >= -2.0 and f_spybollinger_today >= 1.5:
                    df_events[s_sym].ix[ldt_timestamps[i]] = 1
                    # create order file
                    writer = csv.writer(of)
                    event_date = ldt_timestamps[i]
                    writer.writerow([event_date.year,event_date.month,event_date.day,s_sym,"Buy",100])
                    if (i+5 >= len(ldt_timestamps)) :
                        event_date = ldt_timestamps[len(ldt_timestamps)-1]
                    else:
                        event_date = ldt_timestamps[i+5]
                    writer.writerow([event_date.year,event_date.month,event_date.day,s_sym,"Sell",100])
                    cnt = cnt+1
                    print "bollinger Event occurred on " + str(ldt_timestamps[i]) + " for " + s_sym + " " + str(f_symbollinger_yest) + " -> " + str(f_symbollinger_today) + " with " + str(f_spybollinger_today)
    return df_events,cnt


def find_bollinger(d_close, ls_symbols, ldt_timestamps):
    Bollinger_val = copy.deepcopy(d_close)
    for s_sym in ls_symbols:
        rmean = pd.rolling_mean(d_close[s_sym], 20)        
        rstd = pd.rolling_std(d_close[s_sym], 20)        
        upper = rmean + rstd        
        lower = rmean - rstd
        
        Bollinger_val[s_sym] = (d_close[s_sym] - rmean) / (rstd)
        #print Bollinger_val[s_sym]
#    Bollinger_val = Bollinger_val.fillna(method = 'ffill')
#    Bollinger_val = Bollinger_val.fillna(method = 'bfill')
#    Bollinger_val = Bollinger_val.fillna(0.0)
        
    return Bollinger_val
#    for i in range(len(bollinger_vals.index)):
#        date = bollinger_vals.index[i]
#        value = bollinger_vals.ix[date]
#        print repr(date) + " " + repr(value)
    # Plotting the plot of daily returns
#    plt.clf()
#    plt.plot(ldt_timestamps, Bollinger_val)  # Bollinger_val during the period
#    plt.axhline(y=0, color='r')
#    plt.legend(['GOOG Bollinger'],'upper left')
#    plt.ylabel('Stock Value')
#    plt.xlabel('Date')
    #plt.vlines(ldt_timestamps, 0, 700, color='gray', linestyles='solid')
#    plt.savefig('bollinger-value.pdf', format='pdf')
    
    
    # Plotting the plot of daily returns
#    plt.clf()
#    plt.plot(ldt_timestamps, na_price)  # price during the period
#    plt.plot(ldt_timestamps, rmean)  # rolling mean during the period
#    plt.plot(ldt_timestamps, upper)  # upper band during the period
#    plt.plot(ldt_timestamps, lower)  # lower band during the period
#    plt.axhline(y=0, color='r')
#    plt.legend(['GOOG','SMA','Bollinger Upper Band','Bollinger Lower Band'],'lower right')
#    plt.ylabel('Stock Value')
#    plt.xlabel('Date')
    #plt.vlines(ldt_timestamps, 0, 700, color='gray', linestyles='solid')
#    plt.savefig('bollinger.pdf', format='pdf')

def loadData(ldt_timestamps):

    # Creating an object of the dataaccess class with Yahoo as the source.
    c_dataobj = da.DataAccess('Yahoo')
    #c_dataobj = da.DataAccess('Yahoo', cachestalltime=0)

    portfolio_symbols = c_dataobj.get_symbols_from_list('sp5002012')
    portfolio_symbols.append('SPY')

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
    d_close = d_data['close']

    return portfolio_symbols, d_data, d_close, len(ldt_timestamps)


if __name__ == '__main__':
    main()