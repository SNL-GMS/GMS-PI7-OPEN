#You must have matplotlib installed to use this script.
# Plots an command line literal array
# Example usage: plython3 waveform_plotter.py "[1,-2,5,-3]" 

import ast
import sys
import matplotlib.pyplot as plt

waveformvalues = ast.literal_eval(sys.argv[1])
plt.plot(waveformvalues)
plt.ylabel('Samples')
plt.xlabel('Time')
plt.show()
