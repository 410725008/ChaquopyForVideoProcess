import numpy as np

def test_image(array,width,height):
    array = np.array(array,dtype='uint8')+12
    array_size = int(len(array)/4)
    array = array.reshape((height,width,4))[:,:,0:3]





