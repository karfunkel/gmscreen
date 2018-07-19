package org.aklein.rpgscreen.handler

import com.google.inject.Inject
import groovy.json.JsonSlurper
import org.aklein.rpgscreen.GCD
import org.aklein.rpgscreen.entities.Permission
import org.aklein.rpgscreen.security.GCDAnyPermissionAuthorizer
import org.pac4j.oauth.client.Google2Client
import org.pac4j.oauth.profile.google2.Google2Profile
import ratpack.groovy.handling.GroovyChainAction
import ratpack.pac4j.RatpackPac4j

import static ratpack.jackson.Jackson.json

class AdminUserChain extends GroovyChainAction {
  @Inject
  GCD gcd

  @Override
  void execute() throws Exception {

    all(RatpackPac4j.requireAuth(Google2Client,
      new GCDAnyPermissionAuthorizer(gcd, "All", "Administration")
    ))

    all {
      byMethod {
        // List all users
        get() { Google2Profile profile ->
          gcd.async().list(Permission).then { entities ->
            render json(entities)
          }
        }

        // Create user
        put() { Google2Profile profile, JsonSlurper jsonSlurper ->
          request.body.map { body ->
            jsonSlurper.parseText(body.text) as Map
          }.map { data ->
            new Permission(name: data.name, permissions: data.permissions)
          }.flatMap { permission ->
            gcd.async().create(permission)
          }.flatMap { id ->
            gcd.async().show(Permission, id)
          }.then { permission ->
            render json(permission)
          }
        }
      }
    }

    prefix(":user") {
      all {
        byMethod {
          // Show user
          get() { Google2Profile profile ->
            Long id = new Long(allPathTokens.user)
            gcd.async().show(Permission, id).then { entity ->
              render json(entity)
            }
          }

          // Replace user
          put() { Google2Profile profile ->
            render "Replace user $allPathTokens.user"
          }

          // Delete user
          delete() { Google2Profile profile ->
            render "Delete user $allPathTokens.user"
          }
        }
      }

      prefix("permission") {
        all {
          byMethod {
            // List all permission of a user
            get() { Google2Profile profile ->
              render "List all permissions of user $allPathTokens.user"
            }

            // Revoke all permissions from a user
            delete() { Google2Profile profile ->
              render "Revoke all permissions from user $allPathTokens.user"
            }
          }
        }
        prefix(":perm") {
          all {
            byMethod {
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
  }
}
