package org.aklein.rpgscreen.security

import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.QueryResults
import com.google.cloud.datastore.StructuredQuery
import org.aklein.rpgscreen.GCD
import org.pac4j.core.authorization.Authorizer
import org.pac4j.core.context.WebContext
import org.pac4j.oauth.profile.google2.Google2Profile

class GCDAnyPermissionAuthorizer<U extends Google2Profile> implements Authorizer<U> {
  GCD gcd
  List<String> permissions = []

  GCDAnyPermissionAuthorizer(GCD gcd, String... permissions) {
    this.permissions.addAll(permissions)
    this.gcd = gcd
  }

  // TODO: Cache??
  @Override
  boolean isAuthorized(WebContext context, U profile) {
    QueryResults<Entity> results = gcd.query {
      kind = "Permissions"
      filter = StructuredQuery.PropertyFilter.eq("id", profile.id)
    }

    if (results) {
      Entity entity = results.next()
      List permissions = entity.getList('permissions')
      return permissions*.get().intersect(this.permissions)
    }
    return false
  }
}
