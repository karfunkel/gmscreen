package org.aklein.rpgscreen

import geb.spock.GebReportingSpec
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import spock.lang.AutoCleanup

class BasicGebSpec extends GebReportingSpec {
  @AutoCleanup
  def aut = new GroovyRatpackMainApplicationUnderTest()

  def setup() {
    URI base = aut.address
    browser.baseUrl = base.toString()
  }

  void "dummy"() {
    expect:
    true
  }

}
