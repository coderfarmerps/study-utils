# coding=utf-8
class Resourceutil():

    def open_file(self, path):
        try:
            file = open(path, 'r')
            return file.read()
        except IOError, e:
            print e
            return ""
        finally:
            if file:
                file.close()

    def get_num(self, path, split_char):
        file_content = self.open_file(path)
        if not file_content:
            return []
        if split_char:
            file_content = file_content.split(split_char)
        return file_content