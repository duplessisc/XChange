/**
 * Copyright (C) 2012 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.service;

import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.utils.Assert;
import com.xeiam.xchange.utils.HttpTemplate;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * <p>
 * Abstract base class to provide the following to exchange services:
 * </p>
 * <ul>
 * <li>Provision of standard specification parsing</li>
 * </ul>
 */
public abstract class BaseExchangeService {

  /**
   * Jackson JSON to Java object mapper
   */
  protected ObjectMapper mapper = new ObjectMapper();

  /**
   * HTTP template to provide data access facilities
   */
  protected HttpTemplate httpTemplate = new HttpTemplate();

  /**
   * The exchange specification containing session-specific information
   */
  protected final ExchangeSpecification exchangeSpecification;

  /**
   * Initialize common properties from the exchange specification
   * 
   * @param exchangeSpecification The exchange specification with the configuration parameters
   */
  protected BaseExchangeService(ExchangeSpecification exchangeSpecification) {

    Assert.notNull(exchangeSpecification, "exchangeSpecification cannot be null");

    this.exchangeSpecification = exchangeSpecification;

  }

  public void setHttpTemplate(HttpTemplate httpTemplate) {
    this.httpTemplate = httpTemplate;
  }
}
