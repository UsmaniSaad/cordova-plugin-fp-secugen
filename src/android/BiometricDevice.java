package sa.com.plugin;

/**
 * Created by 26HW011470-PC on 8/8/2017.
 */

public abstract class BiometricDevice {
    protected String deviceSerial=null;
    protected byte[] wsqImage=null;
    protected boolean initialized=false;

    abstract public long initialize();
    abstract public long capture();
    abstract public long close();

    public String getDeviceSerial() {
        return this.deviceSerial;
    }

    public byte[] getWsqImage() {
        return wsqImage;
    }
    public boolean isInitialized() {
        return initialized;
    }
}
