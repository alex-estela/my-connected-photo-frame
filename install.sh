#! /bin/sh

# assuming this action was already performed before to run this install.sh script
# git clone https://github.com/alex-estela/piframe

cd /home/pi

sudo apt-get update

# sudo apt-get upgrade -y
# sudo apt-get dist-upgrade –y

sudo apt-get install -y tint2 matchbox-window-manager postgresql imagemagick

git clone git://github.com/rg3/youtube-dl
sudo ln -s ~/youtube-dl/youtube_dl/__main__.py /usr/bin/youtube-dl

wget http://steinerdatenbank.de/software/kweb-1.7.0.tar.gz
tar -xzf kweb-1.7.0.tar.gz
cd kweb-1.7.0
./debinstall

echo 'matchbox-window-manager -use_titlebar no -use_cursor no &
xset s noblank
xset -dpms
xset s off
kweb -KAHZJEobhrp+-zgtjnediwxyqcf "file:///home/pi/piframe/target/classes/static/index.html" ' >> /home/pi/kiosk
chmod +x /home/pi/kiosk

sudo sed -i '/fi/asu -l pi -c "xinit /home/pi/kiosk"' /etc/rc.local

sudo sed -i 's/allowed_users=console/allowed_users=anybody/g' /etc/X11/Xwrapper.config

# Config spéciale HDML PI 7
sudo sed -i '/README/alcd_rotate=2' /boot/config.txt

wget http://www-us.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
sudo tar -xzvf apache-maven-3.3.9-bin.tar.gz

echo 'export M2_HOME="/home/pi/apache-maven-3.3.9"
export PATH="$PATH:$M2_HOME/bin" ' >> /home/pi/maven.sh
source /home/pi/maven.sh
sudo mv /home/pi/maven.sh /etc/profile.d/maven.sh


mkdir /home/pi/piframe/tmp
cd piframe
mvn clean install spring-boot:repackage

sudo ln -s /usr/bin/convert /usr/bin/convert-piframe

echo '#! /bin/sh

### BEGIN INIT INFO
# Provides:          piframe-backend
# Required-Start:    $local_fs $network $remote_fs
# Required-Stop:     $local_fs $network $remote_fs
# Should-Start:      $NetworkManager
# Should-Stop:       $NetworkManager
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: init script
# Description:       init script
### END INIT INFO

PIFRAME_DIR="/home/pi/piframe"
PID_FILE=/tmp/piframe.pid
ARGS="-Dconfig=$PIFRAME_DIR/target/classes/inflector.yaml -jar $PIFRAME_DIR/target/piframe.jar"

case "$1" in
  start)
    start-stop-daemon --start --quiet --background --make-pidfile --pidfile $PID_FILE --chdir $PIFRAME_DIR --chuid pi --exec "/usr/bin/java" -- $ARGS
    ;;
  stop)
    start-stop-daemon --stop --quiet --pidfile $PID_FILE
    ;;
  *)
    echo "Usage: sudo /etc/init.d/piframe {start|stop}"
    exit 1
    ;;
esac

exit 0' >> /home/pi/initd-piframe

sudo mv /home/pi/initd-piframe /etc/init.d/piframe
sudo chmod +x /etc/init.d/piframe
sudo update-rc.d piframe defaults

sudo runuser -l postgres -c "psql -c \"CREATE USER piframe WITH PASSWORD 'piframe';\""
sudo runuser -l postgres -c "psql -c \"CREATE DATABASE piframe OWNER piframe;\""


sudo raspi-config --expand-rootfs

# enable console auto login (emulate raspi-config boot behaviour B2)

sudo systemctl set-default multi-user.target
sudo ln -fs /etc/systemd/system/autologin@.service /etc/systemd/system/getty.target.wants/getty@tty1.service


sudo reboot
