package ca.owro.cryptail;

public class Cryptocurrency {

    private String name;
    private int logoId;
    private String symbol;
    private String conversion;
    private String hrPercent;
    private String hrValue;
    private String dayPercent;
    private String dayValue;
    private String dayVolume;
    private String livePrice;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConversion() {
        return conversion;
    }

    public void setConversion(String conversion) {
        this.conversion = conversion;
    }

    public int getLogoId() {
        return logoId;
    }

    public void setLogoId(int logoId) {
        this.logoId = logoId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getHrPercent() {
        return hrPercent;
    }

    public void setHrPercent(String hrPercent) {
        this.hrPercent = hrPercent;
    }

    public String getHrValue() {
        return hrValue;
    }

    public void setHrValue(String hrValue) {
        this.hrValue = hrValue;
    }

    public String getDayPercent() {
        return dayPercent;
    }

    public void setDayPercent(String dayPercent) {
        this.dayPercent = dayPercent;
    }

    public String getDayValue() {
        return dayValue;
    }

    public void setDayValue(String dayValue) {
        this.dayValue = dayValue;
    }

    public String getDayVolume() {
        return dayVolume;
    }

    public void setDayVolume(String dayVolume) {
        this.dayVolume = dayVolume;
    }

    public String getLivePrice() {
        return livePrice;
    }

    public void setLivePrice(String livePrice) {
        this.livePrice = livePrice;
    }
}
