libraries = ['swt.jar', 'ant.jar']

env = Environment(ENV={'LANG': 'en_US.UTF-8'}, JAVACLASSPATH=libraries)

env.Java('classes', 'src')
env.Jar('JZip.jar', ['Manifest.txt', 'classes', 'icons'] + libraries)
