package org.aklein.rpgscreen.handler

import com.google.inject.Inject
import org.aklein.rpgscreen.GCD
import org.aklein.rpgscreen.security.GCDAnyPermissionAuthorizer
import org.pac4j.oauth.client.Google2Client
import org.pac4j.oauth.profile.google2.Google2Profile
import ratpack.groovy.handling.GroovyChainAction
import ratpack.pac4j.RatpackPac4j

abstract class AbstractEntryChain extends GroovyChainAction {
  @Inject
  GCD gcd

  String type

  @Override
  void execute() throws Exception {
    Map<String, List<String>> permissions = security()

    // List all entries
    requireAuth('get', permissions.list)
    get() {
      render "List $type $allPathTokens.game"
    }

    // Create an entry
    requireAuth('put', permissions.create)
    put() { Google2Profile profile ->
      render "Create $type $profile.attributes.displayName $allPathTokens.game"
    }

    // Show an entry
    requireAuth(':id', 'get', permissions.show)
    get(':id') {
      render "Show $type ($allPathTokens.id): $allPathTokens.game"
    }

    // Replace an entry
    requireAuth(':id', 'put', permissions.replace)
    put(':id') { Google2Profile profile ->
      render "Replace $type ($allPathTokens.id): $profile.attributes.displayName $allPathTokens.game"
    }

    // Delete an entry
    requireAuth(':id', 'delete', permissions.delete)
    delete(':id') { Google2Profile profile ->
      render "Delete $type ($allPathTokens.id): $profile.attributes.displayName $allPathTokens.game"
    }
  }

  protected requireAuth(String path = "", String method, List permissions) {
    if (permissions) {
      if (path) {
        "$method"(path, RatpackPac4j.requireAuth(Google2Client, new GCDAnyPermissionAuthorizer(gcd, permissions as String[])))
      } else {
        "$method"(RatpackPac4j.requireAuth(Google2Client, new GCDAnyPermissionAuthorizer(gcd, permissions as String[])))
      }
    }
  }

  protected Map<String, List<String>> security() {
    return [list   : [],
            create : [],
            show   : [],
            replace: [],
            delete : []
    ]
  }
}

