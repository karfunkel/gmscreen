package org.aklein.rpgscreen

import com.google.cloud.datastore.*
import com.google.inject.Inject
import org.aklein.rpgscreen.config.GoogleConfig
import org.aklein.rpgscreen.entities.Base
import ratpack.exec.Blocking
import ratpack.exec.Promise

class GCD {
  @Lazy
  private GCDAsync ASYNC = { new GCDAsync(gcd: this) }()
  @Inject
  GoogleConfig google
  @Inject
  Datastore datastore
  @Inject
  GCDEntityMapper gem

  final Map<String, KeyFactory> keyFactories = [:].withDefault { kind -> datastore.newKeyFactory().setKind(kind.toString()) }

  protected Key key(String urlSafe) {
    Key.fromUrlSafe(urlSafe)
  }

  protected Key key(String kind, String name) {
    Key.newBuilder(google.projectId, kind, name).build()
  }

  protected Key key(String kind, long id) {
    Key.newBuilder(google.projectId, kind, id).build()
  }

  protected def withTransaction(Closure action) {
    Transaction transaction = datastore.newTransaction()
    try {
      def result = action(transaction)
      transaction.commit()
      return result
    } finally {
      if (transaction.isActive()) {
        transaction.rollback()
      }
    }
  }

  QueryResults<Entity> query(@DelegatesTo(EntityQuery.Builder) Closure querySpec) {
    EntityQuery.Builder builder = Query.newEntityQueryBuilder()
    builder.with(querySpec)
    EntityQuery query = builder.build()
    return datastore.run(query)
  }

  QueryResults<?> query(String gql) {
    Query<?> query = Query.newGqlQueryBuilder(gql).build()
    return datastore.run(query)
  }

  protected Entity get(String kind, long id) {
    return datastore.get(key(kind, id))
  }

  protected Iterator<Entity> get(String kind, Long... ids) {
    return datastore.get(ids.collect { key(kind, it) })
  }

  List<Entity> list(Class<? extends Base> type) {
    QueryResults<Entity> results = query {
      kind = gem.kind(type)
    }
    if (results) {
      List<Entity> entities = results.collect{ gem.fromEntity(it) }
      return entities
    } else {
      return []
    }
  }

  long create(Base entity) {
    KeyFactory keyFactory = keyFactories[entity.kind]
    Key key = datastore.allocateId(keyFactory.newKey())
    datastore.add(gem.toEntity(key, (Class<? extends Base>) entity.getClass(), entity))
    return key.id
  }

  public <T extends Base> T show(Class<T> type, long id) {
    return gem.fromEntity(get(gem.kind(type), id))
  }

  public <T extends Base> List<T> show(Class<T> type, Long... ids) {
    return get(gem.kind(type), ids).collect { gem.fromEntity(it) }
  }

  void replace(Base entity) {
    datastore.put(gem.toEntity((Class<? extends Base>) entity.getClass(), entity))
  }

  void delete(Base entity) {
    datastore.delete(key(entity.kind, entity.id))
  }

  void delete(Class<? extends Base> type, long id) {
    datastore.delete(key(gem.kind(type), id))
  }

  boolean update(long id, Base template) {
    KeyFactory keyFactory = keyFactories[template.kind]
    withTransaction { Transaction transaction ->
      Entity e = transaction.get(keyFactory.newKey(id))
      if (e)
        transaction.put(gem.updateEntity(e, template))
      return e
    }
  }

  GCDAsync async() {
    return ASYNC
  }

  class GCDAsync {
    @Delegate
    GCD gcd

    Promise<QueryResults<Entity>> query(@DelegatesTo(EntityQuery.Builder) Closure querySpec) {
      Blocking.get {
        return gcd.query(querySpec)
      }
    }

    Promise<QueryResults<Entity>> query(String gql) {
      Blocking.get {
        return gcd.query(gql)
      }
    }

    Promise<List<Entity>> list(Class<? extends Base> entity) {
      Blocking.get {
        return gcd.list(entity)
      }
    }

    Promise<Long> create(Base entity) {
      Blocking.get {
        gcd.create(entity)
      }
    }

    public <T extends Base> Promise<T> show(Class<T> type, long id) {
      Blocking.get {
        gcd.show(type, id)
      }
    }

    public <T extends Base> Promise<List<T>> show(Class<T> type, Long... ids) {
      Blocking.get {
        gcd.show(type, ids)
      }
    }

    Promise<Object> replace(Base entity) {
      Blocking.get {
        gcd.replace(entity)
      }
    }

    Promise<Object> delete(Base entity) {
      Blocking.get {
        gcd.delete(entity)
      }
    }

    Promise<Object> delete(Class<? extends Base> type, long id) {
      Blocking.get {
        gcd.delete(type, id)
      }
    }

    Promise<Boolean> update(long id, Base template) {
      Blocking.get {
        gcd.update(id, template)
      }
    }
  }
}
