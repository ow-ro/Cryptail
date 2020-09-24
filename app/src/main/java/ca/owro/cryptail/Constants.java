package ca.owro.cryptail;

import ca.owro.cryptail.R;

final class Constants {

    /*
    * Cryptocompare
     */

    static final String CRYPTO_API_KEY =
            "88e3833d661a92a59979446ae19cac7d77f461dad108f3efa45e50b35adc7ea2";
    static final String CRYPTO_API_TEST_URL =
            "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=USD&apikey=" + CRYPTO_API_KEY;

    static final String CRYPTO_TEMP_URL =
            "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=LTC,EOS,BNB,XMR&tsyms=USD&apikey=88e3833d661a92a59979446ae19cac7d77f461dad108f3efa45e50b35adc7ea2";

    /*
    * Mailgun
     */

    static final String MAIL_API_KEY =
            "a3e4a9dbbb3f31245862ce783c773184-65b08458-9b855839";
    static final String MAIL_DOMAIN_NAME =
            "sandboxddef7fe622d941e390456fcb90742aca.mailgun.org";
    static final String MAIL_TO_EMAIL =  // verified on Mailgun
            "roberts.7@icloud.com";

    /*
    * Cryptos
      */

    static final String[] CRYPTO_NAMES =
            {"Bitcoin","Ethereum","Ripple XRP","Bitcoin Cash","Bitcoin SV","Tether","Litecoin","EOS","Binance Coin","Monero","Stellar","Cardano","Tron","Dash","IOTA",
            "NEO","Eth Classic","NEM","Tezos","Zcash","VeChain","Bitcoin Gold","Maker","OmiseGO","0x","Dogecoin","Decred"};
    static final String[] CRYPTO_TICKERS =
            {"BTC","ETH","XRP","BCH","BSV","USDT","LTC","EOS","BNB","XMR","XLM","ADA","TRX","DASH","MIOTA","NEO","ETC","XEM","XTZ","ZEC","VET","BTG","MKR","OMG","ZRX","DOGE","DCR"};
    static final Integer[] CRYPTO_LOGOS =
                    {R.drawable.ic_bitcoin_btc_logo,R.drawable.ic_ethereum_eth_logo,R.drawable.ic_xrp_xrp_logo,R.drawable.ic_bitcoin_cash_bch_logo,R.drawable.ic_bitcoin_sv_bsv_logo,
                    R.drawable.ic_tether_usdt_logo,R.drawable.ic_litecoin_ltc_logo,R.drawable.ic_eos_eos_logo,R.drawable.ic_binance_coin_bnb_logo,R.drawable.ic_monero_xmr_logo,
                    R.drawable.ic_stellar_xlm_logo,R.drawable.ic_cardano_ada_logo,R.drawable.ic_tron_trx_logo,R.drawable.ic_dash_dash_logo,R.drawable.ic_iota_miota_logo,
                    R.drawable.ic_neo_neo_logo,R.drawable.ic_ethereum_classic_etc_logo,R.drawable.ic_nem_xem_logo,R.drawable.ic_tezos_xtz_logo,R.drawable.ic_zcash_zec_logo,
                    R.drawable.ic_vechain_vet_logo,R.drawable.ic_bitcoin_gold_btg_logo,R.drawable.ic_maker_mkr_logo,R.drawable.ic_omisego_omg_logo,R.drawable.ic_0x_zrx_logo,
                    R.drawable.ic_dogecoin_doge_logo,R.drawable.ic_decred_dcr_logo};
    static final String[] CRYPTO_SYMBOLS =
            {"Ƀ","Ξ","XRP","BCH","BSV","₮","Ł","EOS","BNB","XMR","XLM","ADA","TRX","DASH","IOTA","NEO","ETC","XEM","XTZ","ZEC","VET","BTG","MKR","OMG","ZRX","DOGE","DCR"};
    static final Integer[] SELECTED_CRYPTOS =
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};



}
