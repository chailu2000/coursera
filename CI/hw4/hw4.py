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
    dt_start = dt.datetime(2008, 1, 1)
    dt_end = dt.datetime(2009, 12, 31)
    ldt_timestamps = du.getNYSEdays(dt_start, dt_end, dt.timedelta(hours=16))

    dataobj = da.DataAccess('Yahoo')
    ls_symbols = dataobj.get_symbols_from_list('sp5002012')
    ls_symbols.append('SPY')
    ls_keys = ['open', 'high', 'low', 'close', 'volume', 'actual_close']
    ldf_data = dataobj.get_data(ldt_timestamps, ls_symbols, ls_keys)
    d_data = dict(zip(ls_keys, ldf_data))
    for s_key in ls_keys:
        d_data[s_key] = d_data[s_key].fillna(method = 'ffill')
        d_data[s_key] = d_data[s_key].fillna(method = 'bfill')
        d_data[s_key] = d_data[s_key].fillna(1.0)

    cnt, fileName = find_events(ls_symbols, d_data)
    print "Num. of events: " 
    print cnt

def find_events(ls_symbols, d_data):
    ''' Finding the event dataframe '''
    df_close = d_data['actual_close']

    print "Finding Events"

    # Time stamps for the event range
    ldt_timestamps = df_close.index

    cnt = 0
    fileName = "orders.csv" 

    with open(fileName, 'w') as of: 
        for s_sym in ls_symbols:
            for i in range(1, len(ldt_timestamps)):
                # Find the price for this timestamp
                f_symprice_today = df_close[s_sym].ix[ldt_timestamps[i]]
                f_symprice_yest = df_close[s_sym].ix[ldt_timestamps[i - 1]]
    
                # Event is found if the symbol is down below 5
                if f_symprice_today < 10 and f_symprice_yest >= 10:
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
                    print "5.0 Event occurred on " + str(ldt_timestamps[i]) + " for " + s_sym + " " + str(f_symprice_yest) + " -> " + str(f_symprice_today)

    return cnt, fileName


if __name__ == '__main__':
    main()
