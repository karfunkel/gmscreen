package ratpack

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import org.aklein.rpgscreen.GCD
import org.aklein.rpgscreen.config.GoogleConfig
import org.aklein.rpgscreen.handler.AdminUserChain
import org.aklein.rpgscreen.handler.PrivateEntryChain
import org.aklein.rpgscreen.handler.PublicEntryChain
import org.pac4j.oauth.client.Google2Client
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.pac4j.RatpackPac4j
import ratpack.session.SessionModule

import java.nio.file.Paths

import static ratpack.groovy.Groovy.ratpack

final Logger log = LoggerFactory.getLogger(this.getClass())

ratpack {
  serverConfig {
    props(getClass().classLoader.getResource('application.properties'))
    props(Paths.get("/${System.properties."user.home"}/.google-cloud/rpgscreen.properties"))
    require("/google", GoogleConfig)
  }

  bindings {
    module SessionModule
    bind AdminUserChain
    bind PrivateEntryChain
    bind PublicEntryChain

    Datastore datastore
    GoogleConfig google = getServerConfig().getAsConfigObject('/google', GoogleConfig).object
    if (google.credentials == null)
      datastore = DatastoreOptions.defaultInstance.service
    else {
      DatastoreOptions options = DatastoreOptions.newBuilder()
        .setProjectId(google.projectId)
        .setCredentials(GoogleCredentials.fromStream(new FileInputStream(google.credentials))).build()
      datastore = options.service
    }
    bindInstance Datastore, datastore

    bind GCD
  }

  handlers { GoogleConfig google ->
    all(
      RatpackPac4j.authenticator(
        new Google2Client(google.key, google.secret)
      )
    )

    prefix("admin") {
      prefix("user", AdminUserChain)
    }

    prefix(":game") {
      prefix("public", PublicEntryChain)
      prefix("private", PrivateEntryChain)
    }

    get() {
      render "Load view"
    }

    get("logout") {
      RatpackPac4j.logout(context).then {
        redirect "/"
      }
    }

    files { dir "static" indexFiles "index.html" }
  }
}
