package org.aklein.rpgscreen.handler

import com.google.inject.Inject
import org.aklein.rpgscreen.GCD
import org.aklein.rpgscreen.security.GCDAnyPermissionAuthorizer
import org.pac4j.oauth.client.Google2Client
import org.pac4j.oauth.profile.google2.Google2Profile
import ratpack.groovy.handling.GroovyChainAction
import ratpack.pac4j.RatpackPac4j

class AdminUserChain extends GroovyChainAction {
  @Inject
  GCD gcd

  @Override
  void execute() throws Exception {
    all(RatpackPac4j.requireAuth(Google2Client,
      new GCDAnyPermissionAuthorizer(gcd, "All", "Administration")
    ))

    // List all users
    get() { Google2Profile profile ->
      render "List users"
    }

    // Create user
    put() { Google2Profile profile ->
      render "Create user $allPathTokens.user"
    }

    prefix(":user") {
      // Show user
      get() { Google2Profile profile ->
        render "Show user $allPathTokens.user"
      }

      // Replace user
      put() { Google2Profile profile ->
        render "Replace user $allPathTokens.user"
      }

      // Delete user
      delete() { Google2Profile profile ->
        render "Delete user $allPathTokens.user"
      }

      prefix("permission") {
        // List all permission of a user
        get() { Google2Profile profile ->
          render "List all permissions of user $allPathTokens.user"
        }

        // Revoke all permissions from a user
        delete() { Google2Profile profile ->
          render "Revoke all permissions from user $allPathTokens.user"
        }

        prefix(":perm") {
          // Grant a permission to the user
          post() { Google2Profile profile ->
            render "Grant permission $allPathTokens.perm to user $allPathTokens.user"
          }

          // Revoke a permission from
          delete() { Google2Profile profile ->
            render "Revoke permission $allPathTokens.perm from user $allPathTokens.user"
          }
        }
      }
    }
  }
}
