package org.aklein.rpgscreen

import org.aklein.rpgscreen.security.Google2ClientFactory
import org.pac4j.oauth.client.Google2Client
import org.pac4j.oauth.profile.google2.Google2Profile
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.guice.BindingsImposition
import ratpack.impose.ForceServerListenPortImposition
import ratpack.impose.ImpositionsSpec
import spock.lang.Shared
import spock.lang.Specification

class BasicSpec extends Specification {
  @Shared
  GroovyRatpackMainApplicationUnderTest aut
  @Shared
  String baseLocation

  def setupSpec() {
    def google2ClientFactoryStub = Stub(Google2ClientFactory)
    def google2ClientStub = Stub(Google2Client)
    def google2MockProfile = Stub(Google2Profile)
    google2ClientStub.getUserProfile(_)  >> google2MockProfile

    google2ClientFactoryStub.create >> google2ClientStub
    aut = new GroovyRatpackMainApplicationUnderTest() {
      @Override
      protected void addImpositions(ImpositionsSpec impositions) {
        impositions.add ForceServerListenPortImposition.of(5050)
        impositions.add BindingsImposition.of { it.bindInstance(Google2ClientFactory, ) }
      }
    }
    URI base = aut.address
    baseLocation = base.toString()
  }

  def cleanupSpec() {
    aut.close()
  }

  void "dummy"() {
    when:
    def res = aut.httpClient.getText("/api/admin/user").toString()
    def strip = (res =~ /(?dimsu)\<head\>.*?\<\/head\>/).replaceAll("")
    strip = (strip =~ /(?dimsu)\<style.*?\>.*?\<\/style\>/).replaceAll("")

    then:
    strip == ""
  }

}
