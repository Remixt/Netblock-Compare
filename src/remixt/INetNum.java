package remixt;

public class INetNum {

    private String ipStartString;
    private String ipEndString;

    public INetNum(String ipRange) {
        String[] ips;
        ips = ipRange.split(" - ");
        ipStartString = ips[0];
        ipEndString = ips[1];
    }

    public INetNum(String startIP, int blockSize) { // use this for database files that don't have the ranges pre-calculated.

        IPConverter ipc = new IPConverter();
        Long longIP = ipc.ipToLong(startIP);
        longIP += blockSize -1;

        ipStartString = startIP;
        ipEndString = ipc.longToIp(longIP);
    }


    public void printIP() {
        System.out.println("Start Ip: " + ipStartString + "\n" + "End Ip  : " + ipEndString);
    }

    public String getIpStartString() {
        return ipStartString;
    }

    public String getIpEndString() {
        return ipEndString;
    }
}
