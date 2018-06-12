package remixt;

public class INetNum {

    private String ipStartString;
    private String ipEndString;

    public INetNum(String ip){
        ipStartString = ip.substring(16,30);
        ipEndString = ip.substring(33);
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
