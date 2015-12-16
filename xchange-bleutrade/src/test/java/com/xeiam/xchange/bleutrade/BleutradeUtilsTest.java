package com.xeiam.xchange.bleutrade;

import com.xeiam.xchange.currency.CurrencyPair;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class BleutradeUtilsTest {

  @Test
  public void shouldConvertCurrencyPairToString() throws Exception {
    assertThat(BleutradeUtils.toPairString(CurrencyPair.BTC_AUD)).isEqualTo("BTC_AUD");
    assertThat(BleutradeUtils.toPairString(new CurrencyPair("BLEU", "AUD"))).isEqualTo("BLEU_AUD");
  }

  @Test
  public void shouldConvertStringToCurrencyPair() throws Exception {
    assertThat(BleutradeUtils.toCurrencyPair("BTC_AUD")).isEqualTo(CurrencyPair.BTC_AUD);
    assertThat(BleutradeUtils.toCurrencyPair("BLEU_AUD")).isEqualTo(new CurrencyPair("BLEU", "AUD"));
  }

  @Test
  public void shouldConvertStringToDate() throws Exception {
    assertThat(BleutradeUtils.toDate("2015-12-14 11:27:16.323").getTime()).isEqualTo(1450092436323L);
    assertThat(BleutradeUtils.toDate("2015-12-14 11:15:25").getTime()).isEqualTo(1450091725000L);
    assertThat(BleutradeUtils.toDate("yyyy-MM-dd").getTime()).isEqualTo(0);
    assertThat(BleutradeUtils.toDate("").getTime()).isEqualTo(0);
  }
}
