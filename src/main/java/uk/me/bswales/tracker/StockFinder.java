package uk.me.bswales.tracker;

import uk.me.bswales.tracker.source.ISource;

import java.util.List;

public class StockFinder {

    private List<ISource> sources;

    public StockFinder()
    {
        sources = SourceFactory.getSources();
    }

    public static void main(String[] args) {
        StockFinder stockFinder = new StockFinder();
        stockFinder.printStocks();
    }

    private void printStocks()
    {

    }
}
