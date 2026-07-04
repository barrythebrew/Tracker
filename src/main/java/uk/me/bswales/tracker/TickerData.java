package uk.me.bswales.tracker;

import java.math.BigDecimal;

public class TickerData {

    private String ticker;
    private String source;
    private int averageDailyRange;
    private BigDecimal priceCurrent;
    private BigDecimal fiveDayHigh;
    private BigDecimal fiveDayLow;
    private int volume;
    private BigDecimal close;
    private BigDecimal oneMonthPrice;
    private BigDecimal threeMonthPrice;
    private BigDecimal sixMonthPrice;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getAverageDailyRange() {
        return averageDailyRange;
    }

    public void setAverageDailyRange(int averageDailyRange) {
        this.averageDailyRange = averageDailyRange;
    }

    public BigDecimal getPriceCurrent() {
        return priceCurrent;
    }

    public void setPriceCurrent(BigDecimal priceCurrent) {
        this.priceCurrent = priceCurrent;
    }

    public BigDecimal getFiveDayHigh() {
        return fiveDayHigh;
    }

    public void setFiveDayHigh(BigDecimal fiveDayHigh) {
        this.fiveDayHigh = fiveDayHigh;
    }

    public BigDecimal getFiveDayLow() {
        return fiveDayLow;
    }

    public void setFiveDayLow(BigDecimal fiveDayLow) {
        this.fiveDayLow = fiveDayLow;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public BigDecimal getOneMonthPrice() {
        return oneMonthPrice;
    }

    public void setOneMonthPrice(BigDecimal oneMonthPrice) {
        this.oneMonthPrice = oneMonthPrice;
    }

    public BigDecimal getThreeMonthPrice() {
        return threeMonthPrice;
    }

    public void setThreeMonthPrice(BigDecimal threeMonthPrice) {
        this.threeMonthPrice = threeMonthPrice;
    }

    public BigDecimal getSixMonthPrice() {
        return sixMonthPrice;
    }

    public void setSixMonthPrice(BigDecimal sixMonthPrice) {
        this.sixMonthPrice = sixMonthPrice;
    }
}