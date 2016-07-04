package com.sonictest.addcbuilder.Logic;

import com.jcabi.ssh.SSHByPassword;
import com.jcabi.ssh.Shell;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by Sander Rõuk on 29.06.16.
 */

public class ADDCBuilder {
    private TextArea console;

    public ADDCBuilder(TextArea console) {
        this.console = console;
    }

    public String buildAddc(String guestIP, String hostname, String corporation, String guestAdminUsername, String guestAdminPassword, String domainAdminPassword, String netmask, String gateway, String upstreamDNS, String domainURL) throws UnknownHostException, IOException {
        String host = guestIP;
        String username = guestAdminUsername;
        String passwd = guestAdminPassword;
        String domain = corporation + "." + domainURL;
        String upstream = upstreamDNS;
        String domainPass = domainAdminPassword;

        //EXEC
        Shell shell = new SSHByPassword(host, 22, username, passwd);
        String base = "echo " +  passwd + " | sudo -S ";
        Shell.Safe safe = new Shell.Safe(shell);
        //Shell.Verbose verbose = new Shell.Verbose(safe);
        Shell.Plain plain = new Shell.Plain(safe);

        write("Updating packages and running upgrades.");
        write(plain.exec(base + "apt update"));
        write(plain.exec(base + "apt upgrade -y"));

        //install packages - REQUIRES krb5-user package and openssh-server ofc

        write("Installing new packages.");
        write(plain.exec(base + "apt install attr build-essential libacl1-dev libattr1-dev libblkid-dev libgnutls-dev libreadline-dev python-dev libpam0g-dev " +
                "python-dnspython gdb pkg-config libpopt-dev libldap2-dev dnsutils libbsd-dev attr docbook-xsl libcups2-dev acl ntp ntpdate winbind -y"));

        //edit krb5.conf
        write("Editing krb5.conf");
        String existingDomain = plain.exec("cat /etc/krb5.conf | grep 'default_realm'");
        existingDomain = existingDomain.substring(existingDomain.indexOf("=") + 1).trim();
        String existingServer = plain.exec("cat /etc/krb5.conf | grep '" + existingDomain + "'");
        existingServer = existingServer.substring(existingServer.indexOf("kdc"));
        existingServer = existingServer.substring(existingServer.indexOf("=") + 1, existingServer.indexOf(".")).trim();
        plain.exec(base + "sed -i s/'" + existingDomain.substring(existingDomain.indexOf(".") + 1) + "'/'" + domain.toUpperCase() + "'/g /etc/krb5.conf");
        plain.exec(base + "sed -i s/'" + existingServer + "'/'" + hostname.toUpperCase() + "'/g /etc/krb5.conf");

        //edit fstab
        write("Editing fstab");
        plain.exec(base + "sed -i s/'errors=remount-ro'/'user_xattr,acl,barrier=1,errors=remount-ro'/g /etc/fstab");

        // '/etc/network/interfaces' change 1
        write("Editing network/interfaces");
        String interfacesAddress = "address " + host;
        String interfacesNetmask = "netmask " + netmask;
        String interfacesGateway = "gateway " + gateway + "\n";
        String interfacesNameserv = "dns-nameservers " + host + " " + upstream + "\n";
        String interfacesSearch = "dns-search " + domain;

        plain.exec(base + "sed -i s/'inet dhcp'/'inet static'/g /etc/network/interfaces");
        plain.exec(base + "bash -c " + '"'+ "echo '" + interfacesAddress + "' >> /etc/network/interfaces" + '"');
        plain.exec(base + "bash -c " + '"'+ "echo '" + interfacesNetmask + "' >> /etc/network/interfaces" + '"');
        plain.exec(base + "bash -c " + '"'+ "echo '" + interfacesGateway+ "' >> /etc/network/interfaces" + '"');
        plain.exec(base + "bash -c " + '"'+ "echo '" + interfacesNameserv + "' >> /etc/network/interfaces" + '"');
        plain.exec(base + "bash -c " + '"'+ "echo '" + interfacesSearch+ "' >> /etc/network/interfaces" + '"');

        //write new hostname
        plain.exec(base + "sh -c " + '"' + "echo '" + hostname + "' > /etc/hostname" + '"');

        // /etc/hosts edit
        write("Editing hosts");
        String currentHostname = plain.exec("hostname").trim();
        String newHostname = hostname + "." + domain + "\t" + hostname;
        plain.exec(base + "sed -i s/'" + currentHostname + "'/'" + newHostname + "'/g /etc/hosts");

        // Setting NTP
        write("Setting NTP");
        write(plain.exec(base + "service ntp stop"));
        write(plain.exec(base + "ntpdate -B 0.ubuntu.pool.ntp.org"));
        write(plain.exec(base + "service ntp start"));


        // Reboot
        try{
            plain.exec(base + "reboot");
        } catch (Exception e){
            write("Rebooting");
        }

        //Try reconnecting again.

        while (true) {
            try {
                shell = new SSHByPassword(host, 22, username, passwd);
                plain = new Shell.Plain(shell);
                plain.exec("echo reboot done | wall");
                break;
            } catch (Exception e){
            }
        }
        write("Reboot done, connection resuming");

        //Download samba and smbclient, mv smb conf, provision domain
        write("Installing samba and provisioning domain.");
        write(plain.exec(base + "apt install samba -y"));
        write(plain.exec(base + "apt install smbclient -y"));
        plain.exec(base + "mv /etc/samba/smb.conf /etc/samba/smb.conf.orig");
        write(plain.exec(base + "samba-tool domain provision --use-rfc2307 --realm=" + domain.toUpperCase() + " --domain=" + corporation.toUpperCase() + " --adminpass=" + domainPass + " --server-role=dc --dns-backend=SAMBA_INTERNAL"));
        plain.exec(base + "sed -i s/'" + host + "'/'" + upstream + "'/g /etc/samba/smb.conf");
        plain.exec(base + "sed -i s/'dns-nameservers " + host + " " + upstream + "'/'dns-nameservers " + host + "'/g /etc/network/interfaces");

        //Set up kerberos and restart
        write("Setting up kerberos");
        plain.exec(base + "mv /etc/krb5.conf /etc/krb5.conf.orig");
        plain.exec(base + "ln -sf /var/lib/samba/private/krb5.conf /etc/krb5.conf");
        write("Rebooting");
        try{
            plain.exec(base + "reboot");
        } catch (Exception e){
            write("rebooting");
        }

        while (true) {
            try {
                shell = new SSHByPassword(host, 22, username, passwd);
                plain = new Shell.Plain(shell);
                plain.exec("echo reboot done | wall");
                break;
            } catch (Exception e){
            }
        }
        write("Reboot done, connection resuming");

        //Testing DNS
        write("\n\nTesting DNS");
        write(plain.exec("host -t SRV _ldap._tcp." + domain));
        write(plain.exec("host -t SRV _kerberos._udp." + domain));
        write(plain.exec("host -t A " + hostname + "." + domain));


        return "The process has finished.";
        //plain.exec(base + "usermod -a -G sambashare " + corporation);
    }

    public void write(final String s) throws IOException {
        Platform.runLater(new Runnable() {
            public void run() {
                console.appendText(s + "\n");
            }
        });
    }

}
