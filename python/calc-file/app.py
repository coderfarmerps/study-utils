#!/usr/bin/env python
# coding=utf-8
import sys
from file import Resourceutil
from calc import Calcutil

'''
 参数：
    1. 要执行的python文件 app.py
    2. 数据存放的文件路径 ~/num.txt
    3. 数字拆分符号 ,
    4. 计算方法 + - '*' /
'''
path = sys.argv[1]
if len(sys.argv) == 3:
    split_char = '\n'
    symbol = sys.argv[2]
elif len(sys.argv) == 4:
    split_char = sys.argv[2]
    symbol = sys.argv[3]

print "file path: " + path
print "split_char: " + split_char
print "calc method: " + symbol

resourceutil = Resourceutil()
calcutil = Calcutil(resourceutil.get_num(path, split_char))

print "result: " + str(calcutil.calc(symbol))

