package org.aklein.rpgscreen.security

import org.aklein.rpgscreen.config.GoogleConfig
import org.pac4j.oauth.client.Google2Client

class Google2ClientFactory {
  Google2Client create(GoogleConfig google) {
    new Google2Client(google.key, google.secret)
  }
}
