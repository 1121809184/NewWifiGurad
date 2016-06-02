package com.sharedream.wifiguard.utils;

import android.net.DhcpInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;

public final class Arp {
    private DhcpInfo dhcpInfo;
    private String gatewayMac;

    public Arp(DhcpInfo dhcpInfo) {
        super();
        this.gatewayMac = null;
        this.dhcpInfo = dhcpInfo;
    }

    public final boolean detect() {
        int count = 2500;
        while(true) {
            if (count <= 0) {
                break;
            }

            if (this.detectArpAttack()) {
                break;
            }

            count += -100;
            try {
            	Thread.sleep(100);
            } catch(InterruptedException exception) {
                exception.printStackTrace();
            }
        }

        return count <= 0;
    }

    private static String formatIP(int ip) {
        return (ip & 255) + "." + (ip >> 8 & 255) + "." + (ip >> 16 & 255) + "." + (ip >> 24 & 255);
    }

    private boolean detectArpAttack() {
        String gatewayIp = formatIP(this.dhcpInfo.gateway);
        HashMap<String, String> map = new HashMap<String, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/net/arp"));
            // Skip over the line bearing colum titles
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("[ ]+");
                if (tokens.length < 6) {
                    continue;
                }
                // ARP column format is
                // Address HWType HWAddress Flags Mask IFace
                String ip = tokens[0];
                String mac = tokens[3];
/*                    if (remoteIpAddress.equals(ip)) {
                        macAddress = mac;
                        break;
                    }*/
				if (ip != null && mac != null) {
					map.put(ip, mac);
				} else {
					continue;
				}
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
                e.printStackTrace();
			}
		}

        String mac = map.get(gatewayIp);
        if (mac == null || mac.equals("00:00:00:00:00:00")) {
            return false;
        } else {
            if (this.gatewayMac == null) {
                this.gatewayMac = mac;
            } else if(!this.gatewayMac.equals(mac)) {
                return true;
            }

            Iterator<?> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                HashMap.Entry entry = (HashMap.Entry)iter.next();
                if (!entry.getKey().equals(gatewayIp) && ((String)entry.getValue()).equalsIgnoreCase(mac)) {
                    return true;
                }
            }

            return false;
        }
    }
}

