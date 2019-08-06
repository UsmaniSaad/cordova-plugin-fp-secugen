package sa.com.plugin;

/**
 * Created by 26HW011470-PC on 8/13/2017.
 */

public class ImageData {
    private long resultCode;
    private byte[] buffer;
    private byte[] wsqBuffer;
    private int quality;

    public ImageData(long resultCode, byte[] buffer, byte[] wsqBuffer, int quality) {
        this.resultCode = resultCode;
        this.buffer = buffer;
        this.wsqBuffer = wsqBuffer;
        this.quality = quality;
    }

    public ImageData(long resultCode) {
        this.resultCode = resultCode;
    }

    public ImageData(){}

    public long getResultCode() {
        return resultCode;
    }

    public void setResultCode(long resultCode) {
        this.resultCode = resultCode;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public byte[] getWsqBuffer() {
        return wsqBuffer;
    }

    public void setWsqBuffer(byte[] wsqBuffer) {
        this.wsqBuffer = wsqBuffer;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }
}
