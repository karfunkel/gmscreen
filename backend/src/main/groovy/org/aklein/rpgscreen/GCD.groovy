package org.aklein.rpgscreen

import com.google.cloud.datastore.*
import com.google.inject.Inject
import org.aklein.rpgscreen.config.GoogleConfig
import org.aklein.rpgscreen.entities.Base
import ratpack.exec.Blocking
import ratpack.exec.Promise

class $_$ {}

class GCD {
  @Inject
  GoogleConfig google
  @Inject
  Datastore datastore

  protected
  final Map<String, KeyFactory> keyFactories = [:].withDefault { kind -> datastore.newKeyFactory().setKind(kind) }

  protected List<String> generatedFields = $_$.declaredFields*.name

  private final static List<Class<?>> WRAPPERS = [Boolean, Character, Byte, Short, Integer, Long, Float, Double, Void]

  protected Key key(String urlSafe) {
    Key.fromUrlSafe(urlSafe)
  }

  protected Key key(String kind, String name) {
    Key.newBuilder(google.projectId, kind, name).build()
  }

  protected Key key(String kind, long id) {
    Key.newBuilder(google.projectId, kind, id).build()
  }

  protected List<String> entityFields(Class<? extends Base> type) {
    return (type.declaredFields*.name - generatedFields - ['entity', 'id']).findAll {
      !it.startsWith('this$')
    }
  }

  protected Entity toEntity(Key key = null, Class<? extends Base> type, Base instance) {
    String entity = instance.kind
    long id = instance.id
    Key newKey = key ?: Key.newBuilder(google.projectId, entity, id).build()
    Entity.Builder builder = Entity.newBuilder(newKey)
    entityFields(type).each { name ->
      builder.set(name, instance."$name")
    }
    return builder.build()
  }

  protected <T extends Base> T fromEntity(Class<T> type, Entity entity) {
    Base instance = type.newInstance()
    if (instance.kind != entity.key.kind) {
      throw new IllegalArgumentException("Type $type.canonicalName does not fit entity kind $entity.key.kind")
    }
    instance.id = entity.key.id
    entityFields(type).each { name ->
      instance."$name" = entity.getValue(name).get
    }
    return instance
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

  List<Entity> list(String _kind) {
    QueryResults<Entity> results = query {
      kind = _kind
    }
    if (results) {
      List<Entity> entities = results.toList()
      return entities
    } else {
      return []
    }
  }

  long create(Base entity) {
    KeyFactory keyFactory = keyFactories[entity.kind]
    Key key = datastore.allocateId(keyFactory.newKey())
    datastore.add(toEntity(key, (Class<? extends Base>) entity.getClass(), entity))
    return key.id
  }

  public <T extends Base> T show(Class<T> type, long id) {
    String kind = type.newInstance().kind
    return fromEntity(type, get(kind, id))
  }

  public <T extends Base> List<T> show(Class<T> type, Long... ids) {
    String kind = type.newInstance().kind
    return get(kind, ids).collect { fromEntity(type, it) }
  }

  void replace(Base entity) {
    datastore.put(toEntity((Class<? extends Base>) entity.getClass(), entity))
  }

  void delete(Base entity) {
    datastore.delete(key(entity.kind, entity.id))
  }

  void delete(Class<? extends Base> type, long id) {
    String kind = type.newInstance().kind
    datastore.delete(key(kind, id))
  }

  private isPrimitiveOrWrapper(Class<?> type) {
    return type.primitive ?: WRAPPERS.contains(type)
  }

  boolean update(long id, Base template) {
    KeyFactory keyFactory = keyFactories[template.kind]
    withTransaction { Transaction transaction ->
      Entity e = transaction.get(keyFactory.newKey(id))
      if (e != null) {
        Entity.Builder builder = Entity.newBuilder(e)
        entityFields((Class<? extends Base>) template.getClass()).each { name ->
          MetaProperty mp = template.metaClass.getMetaProperty(name)
          def value = mp.getProperty(template)
          if (isPrimitiveOrWrapper(mp.type)) {
            if (value)
              builder.set(name, template."$name")
          } else if (value != null)
            builder.set(name, template."$name")
        }
        transaction.put(builder.build())
      }
      return e != null
    }
  }

  GCDAsync async() {
    return new GCDAsync(gcd: this)
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

    Promise<List<Entity>> list(String entity) {
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
