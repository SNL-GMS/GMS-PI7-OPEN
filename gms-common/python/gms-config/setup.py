from setuptools import setup, find_packages

'''
To rebuild, run python3 setup.py
'''

VERSION = '1.0.0'

setup(
    name='gms-config',
    version=VERSION,
    description='A command line application to import and export GMS configuration data from etcd.',
    packages=find_packages(),
    scripts=['bin/gms-config'],
    install_requires=['etcd3==0.8.1',
                      'grpcio==1.12.1',
                      'protobuf==3.6.0',
                      'six==1.11.0',
                      'jproperties==2.0.0',
                      'tenacity==4.12.0'],
    python_requires='>=3'
)
