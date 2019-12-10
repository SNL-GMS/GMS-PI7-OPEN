# -*- coding: utf-8 -*-
# setup.py
# Last update: 10/22/2018

from setuptools import setup, find_packages

'''
To install: python setup.py install
To rebuild: python setup.py build
'''

VERSION = '7.5.1'

setup(name='Master-COI-data-client',
      version=VERSION,
      description='A command line application to retrieve COI data objects and related information. '
                  'Includes an extension script that wraps the client''s functionality.',
      packages=find_packages(),
      include_package_data=True,
      scripts=['coidataclient.py'],
      install_requires=['pandas',
                        'requests',
                        'termcolor',
                        'responses',
                        'matplotlib',
                        'numpy',
                        'python-dateutil',
                        'pyyaml'],

      python_requires='>=3')
