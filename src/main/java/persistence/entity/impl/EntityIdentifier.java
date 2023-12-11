package persistence.entity.impl;

import java.util.Objects;
import persistence.sql.schema.meta.ColumnMeta;
import persistence.sql.schema.meta.ValueMeta;

public class EntityIdentifier {

    private final ColumnMeta columnIdentifier;
    private final ValueMeta valueIdentifier;

    private EntityIdentifier(ColumnMeta columnMetaIdentifier, ValueMeta valueMetaIdentifier) {
        this.columnIdentifier = columnMetaIdentifier;
        this.valueIdentifier = valueMetaIdentifier;
    }

    public static EntityIdentifier fromIdColumnMetaWithValueMeta(ColumnMeta columnMeta, ValueMeta valueMeta) {
        return new EntityIdentifier(columnMeta, valueMeta);
    }

    public static EntityIdentifier fromIdColumnMetaWithValue(ColumnMeta columnMeta, Object id) {
        return fromIdColumnMetaWithValueMeta(columnMeta, ValueMeta.of(id));
    }

    public boolean isEmptyId() {
        return valueIdentifier.getValue() == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityIdentifier that = (EntityIdentifier) o;
        return Objects.equals(columnIdentifier, that.columnIdentifier) && Objects.equals(valueIdentifier,
            that.valueIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnIdentifier, valueIdentifier);
    }
}
