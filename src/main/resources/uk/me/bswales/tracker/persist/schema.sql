CREATE TABLE IF NOT EXISTS ticker_data (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    ticker              TEXT    NOT NULL,
    source              TEXT    NOT NULL,
    average_daily_range INTEGER DEFAULT 0,
    price_current       REAL,
    five_day_high       REAL,
    five_day_low        REAL,
    volume              INTEGER DEFAULT 0,
    close               REAL,
    one_month_price     REAL,
    three_month_price   REAL,
    six_month_price     REAL,
    date_retrieved      TEXT    NOT NULL,
    UNIQUE(ticker, date_retrieved)
);