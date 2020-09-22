# CloudServices

### Requirements

Install Sigar on all linux servers using:
```bash
wget https://netcologne.dl.sourceforge.net/project/sigar/sigar/1.6/hyperic-sigar-1.6.4.tar.gz
tar xvf hyperic-sigar-1.6.4.tar.gz
cd hyperic-sigar-1.6.4

# INSTALL
sudo cp sigar-bin/lib/libsigar-`dpkg --print-architecture`-`uname -s | tr '[:upper:]' '[:lower:]'`.so /usr/lib
```

