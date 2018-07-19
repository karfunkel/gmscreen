package org.aklein.rpgscreen

import com.google.cloud.Timestamp
import com.google.cloud.datastore.*
import com.google.inject.Inject
import org.aklein.rpgscreen.config.GoogleConfig
import org.aklein.rpgscreen.entities.Base
import org.reflections.Reflections

class $_$ {}

class GCDEntityMapper {
  @Inject
  GoogleConfig google
  protected List<String> generatedFields = $_$.declaredFields*.name
  private Reflections reflections = new Reflections("org.aklein.rpgscreen.entities")
  @Lazy
  private Map<String, Class<? extends Base>> types = {
    Set<Class<? extends Base>> classes = reflections.getSubTypesOf(Base)
    return classes.collectEntries { [(it.newInstance().kind): it] }
  }()

  @Lazy
  private Map<Class<? extends Base>, String> kinds = {
    Set<Class<? extends Base>> classes = reflections.getSubTypesOf(Base)
    return classes.collectEntries { [(it): (it.newInstance().kind)] }
  }()

  private final static List<Class<?>> WRAPPERS = [Boolean, Character, Byte, Short, Integer, Long, Float, Double, Void]

  Class<? extends Base> type(String kind) {
    return types[kind]
  }

  String kind(Class<? extends Base> type) {
    return kinds[type]
  }

  Entity toEntity(Key key = null, Class<? extends Base> type, Base instance) {
    String entity = instance.kind
    long id = instance.id
    Key newKey = key ?: Key.newBuilder(google.projectId, entity, id).build()
    Entity.Builder builder = Entity.newBuilder(newKey)
    entityFields(type).each { name ->
      builder.set(name, toValue(instance."$name"))
    }
    return builder.build()
  }

  private isPrimitiveOrWrapper(Class<?> type) {
    return type.primitive ?: WRAPPERS.contains(type)
  }

  Entity updateEntity(Entity e, Base template) {
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
      return builder.build()
    }
  }

  List<String> entityFields(Class<? extends Base> type) {
    return (type.declaredFields*.name - generatedFields - ['kind', 'id']).findAll {
      !it.startsWith('this$')
    }
  }

  Base fromEntity(FullEntity<?> entity) {
    String kind = entity.key.kind
    Class<? extends Base> type = type(kind)
    Base instance = type.newInstance()
    entityFields(type).each { name ->
      instance."$name" = fromValue(entity.getValue(name))
    }
    instance.id = entity.key.id
    return instance
  }

  protected def fromValue(RawValue value) { throw new UnsupportedOperationException("RawValues not supported") }

  protected String fromValue(StringValue value) { return value.get() }

  protected Value toValue(String value) { return (value == null) ? NullValue.of() : StringValue.of(value) }

  protected Boolean fromValue(BooleanValue value) { return value.get() }

  protected Value toValue(Boolean value) { return (value == null) ? NullValue.of() : BooleanValue.of(value) }

  protected Double fromValue(DoubleValue value) { return value.get() }

  protected Value toValue(Double value) { return (value == null) ? NullValue.of() : DoubleValue.of(value) }

  protected Long fromValue(LongValue value) { return value.get() }

  protected Value toValue(Long value) { return (value == null) ? NullValue.of() : LongValue.of(value) }

  protected Date fromValue(TimestampValue value) { return value.get().toDate() }

  protected Value toValue(Date value) { return (value == null) ? NullValue.of() :TimestampValue.of(Timestamp.of(value)) }

  protected byte[] fromValue(BlobValue value) { return value.get().toByteArray() }

  protected Value toValue(Byte[] value) { return (value == null) ? NullValue.of() : BlobValue.of(Blob.copyFrom(value)) }

  protected GCDLatLong fromValue(LatLngValue value) {
    return new GCDLatLong(latitude: value.get().latitude, longitude: value.get().longitude)
  }

  protected Value toValue(GCDLatLong value) {
    return (value == null) ? NullValue.of() : LatLngValue.of(LatLng.of(value.latitude, value.longitude))
  }

  protected GCDKey fromValue(KeyValue value) {
    return new GCDKey(projectId: value.get().projectId, kind: value.get().kind, id: value.get().id)
  }

  protected Value toValue(GCDKey value) {
    return (value == null) ? NullValue.of() : KeyValue.of(Key.newBuilder(value.projectId, value.kind, value.id).build())
  }

  protected Void fromValue(NullValue value) { return null }

  protected Base fromValue(EntityValue value) { return fromEntity(value.get()) }

  protected Value toValue(Base value) { return (value == null) ? NullValue.of() : toEntity(null, (Class<? extends Base>) value.getClass(), value) }

  protected List<Object> fromValue(ListValue value) { return value.get().collect { fromValue(it) } }

  protected Value toValue(List<?> value) { return (value == null) ? NullValue.of() : ListValue.of(value.collect { toValue(it) })}

}

class GCDLatLong {
  double latitude
  double longitude
}

class GCDKey {
  String projectId
  String kind
  long id
}
