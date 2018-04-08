#!/usr/bin/python
# -*- coding: utf-8 -*-

import os
import time
from qiniu import Auth, put_file
from AppKit import NSPasteboard, NSPasteboardTypePNG, NSPasteboardTypeTIFF


access_key = 'xx' # AK
secret_key = 'xx' # SK

bucket_name = 'xx' # 七牛空间名

q = Auth(access_key, secret_key)

def upload_qiniu(path):
    ''' upload file to qiniu'''
    dirname, filename = os.path.split(path)
    key = '%s' % filename # upload to qiniu's markdown dir

    token = q.upload_token(bucket_name, key)
    ret, info = put_file(token, key, path, check_crc=True)
    return ret != None and ret['key'] == key

def get_paste_img_file():
    pb = NSPasteboard.generalPasteboard()
    data_type = pb.types()
    # if img file
    print data_type
    now = int(time.time() * 1000) # used for filename
    if NSPasteboardTypePNG in data_type:
        # png
        data = pb.dataForType_(NSPasteboardTypePNG)
        filename = '%s.png' % now
        filepath = '/tmp/%s' % filename
        ret = data.writeToFile_atomically_(filepath, False)
        if ret:
            return filepath
    elif NSPasteboardTypeTIFF in data_type:
        # tiff
        data = pb.dataForType_(NSPasteboardTypeTIFF)
        filename = '%s.tiff' % now
        filepath = '/tmp/%s' % filename
        ret = data.writeToFile_atomically_(filepath, False)
        if ret:
            return filepath
    elif NSPasteboardTypeString in data_type:
        # string todo, recognise url of png & jpg
        pass


if __name__ == '__main__':
	#get_paste_img_file()
	#url = "http://7sbqce.com1.z0.glb.clouddn.com/markdown"
	url = "http://7xpc60.com1.z0.glb.clouddn.com"


	img_file = get_paste_img_file()
	if img_file:
	    # has file
	    ret = upload_qiniu(img_file)
	    if ret:
	        # upload success
	        name = os.path.split(img_file)[1]
	        #markdown_url = "![](%s/%s?imageMogr2/thumbnail/!100p/quality/100!)" % (url, name)
	        markdown_url = "%s/%s" % (url, name)
		# make it to clipboard
	        os.system("echo '%s' | pbcopy" % markdown_url)
	        #os.system('osascript -e \'tell application "System Events" to keystroke "v" using command down\'')
	    else: 
	    	print "upload_failed"
	else:
	    print "get img file failed"
