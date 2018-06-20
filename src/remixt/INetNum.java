package remixt;

public class INetNum {

    private String ipStartString;
    private String ipEndString;

    public INetNum(String ip){
        String[] ips;
        ips = ip.split(" - ");
        ipStartString = ips[0];
        ipEndString = ips[1];
    }
    public void printIP(){
        System.out.println("Start Ip: " + ipStartString + "\n" + "End Ip  : " + ipEndString);
    }

    public String getIpStartString(){
        return ipStartString;
    }

    public String getIpEndString() {
        return ipEndString;
    }
}
