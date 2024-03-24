package persistence.jpa;

import persistence.entity.EntityKey;
import persistence.entity.EntityMetaData;

import java.util.HashMap;
import java.util.Map;

public class SnapShot {
    private final Map<EntityKey, EntityMetaData> snapshot;

    public SnapShot() {
        this.snapshot = new HashMap<>();
    }


    public EntityMetaData save(EntityKey entityKey, EntityMetaData entity) {
        return snapshot.put(entityKey, entity);
    }

    public EntityMetaData get(EntityKey entityKey) {
        return snapshot.get(entityKey);
    }

    public void clear() {
        snapshot.clear();
    }
}
