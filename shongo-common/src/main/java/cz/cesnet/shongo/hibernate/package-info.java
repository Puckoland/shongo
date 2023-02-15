/**
 * Package contains classes for persisting Joda Time classes.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@TypeDefs({
        @TypeDef(name = PersistentLocale.NAME, typeClass = PersistentLocale.class),
        @TypeDef(name = PersistentDateTime.NAME, typeClass = PersistentDateTime.class),
        @TypeDef(name = PersistentDateTimeWithZone.NAME, typeClass = PersistentDateTimeWithZone.class),
        @TypeDef(name = PersistentDateTimeZone.NAME, typeClass = PersistentDateTimeZone.class),
        @TypeDef(name = PersistentLocalDate.NAME, typeClass = PersistentLocalDate.class),
        @TypeDef(name = PersistentPeriod.NAME, typeClass = PersistentPeriod.class),
        @TypeDef(name = PersistentInterval.NAME, typeClass = PersistentInterval.class),
        @TypeDef(name = PersistentReadablePartial.NAME, typeClass = PersistentReadablePartial.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
        @TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class),
}) package cz.cesnet.shongo.hibernate;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

