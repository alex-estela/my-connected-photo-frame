#! /bin/sh

sudo apt-get update

# sudo apt-get upgrade -y
# sudo apt-get dist-upgrade –y

sudo apt-get install -y apt-transport-https tint2 matchbox-window-manager postgresql imagemagick youtube-dl

ginstall-ytdl

wget http://steinerdatenbank.de/software/kweb-1.7.0.tar.gz
tar -xzf kweb-1.7.0.tar.gz
cd kweb-1.7.0
./debinstall

echo 'matchbox-window-manager -use_titlebar no -use_cursor no &
xset s noblank
xset -dpms
xset s off
kweb -KAHZJEobhrp+-zgtjnediwxyqcf "file:///home/pi/piframe/static/index.html" ' >> /home/pi/kiosk
chmod +x /home/pi/kiosk

sudo vi /etc/xdg/lxsession/LXDE-pi/autostart

sudo sed -i '/exit 0/isu -l pi -c "xinit ./kiosk"' /etc/rc.local

sudo sed -i '/README/alcd_rotate=2' /boot/config.txt

# sudo raspi-config et mettre le demarrage console sans login ?

sudo echo '#! /bin/sh

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
ARGS="-jar $PIFRAME_DIR/piframe.jar"

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

exit 0' >> /home/pi/kiosk

sudo chmod +x /etc/init.d/piframe
sudo update-rc.d piframe defaults

sudo runuser -l postgres -c "psql -c \"CREATE USER piframe WITH PASSWORD 'piframe';\""
sudo runuser -l postgres -c "psql -c \"CREATE DATABASE piframe OWNER piframe;\""

mkdir /home/pi/piframe
mkdir /home/pi/piframe/tmp

sudo ln -s /usr/bin/convert /usr/bin/imagemagick-convert

# uploader piframe.jar
# uploader dossier static

