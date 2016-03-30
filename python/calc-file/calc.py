# coding=utf-8
class Calcutil():
    def __init__(self, num_arr):
        self.num_arr = num_arr
        self.num_check()
        self.calc_symbol = {
            "+": self.add,
            "-": self.sub,
            "*": self.mul,
            "/": self.divide
        }

    def add(self):
        result = 0.0
        try:
            for num in self.num_arr:
                result = result + float(num)
        except Exception, e:
            print e
        return result

    def sub(self):
        result = float(self.num_arr[0])
        self.num_arr.remove(self.num_arr[0])
        try:
            for num in self.num_arr:
                result = result - float(num)
        except Exception, e:
            print e
        return result

    def mul(self):
        result = float(self.num_arr[0])
        self.num_arr.remove(self.num_arr[0])
        try:
            for num in self.num_arr:
                result = result * float(num)
        except Exception, e:
            print e
        return result

    def divide(self):
        result = float(self.num_arr[0])
        self.num_arr.remove(self.num_arr[0])
        try:
            for num in self.num_arr:
                result = result / float(num)
        except Exception, e:
            print e
        return result

    def calc(self, symbol):
        method = self.calc_symbol[symbol]
        if method:
            return method()

    def num_check(self):
        nums = []
        for num in self.num_arr:
            try:
                nums.append(float(num.strip()))
            except Exception, e:
                print "num error: " + num
        self.num_arr = nums
        print self.num_arr
