env = Environment(ENV={'LANG': 'en_US.UTF-8'},
                  JAVACLASSPATH=['swt.jar', 'ant.jar'],
                  JARCHDIR='classes')

env.Java('classes', 'src')
env.Jar('JZip.jar', ['classes', 'Manifest.txt'])
