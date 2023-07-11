package cz.cesnet.shongo;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * {@link PersistentObject} with {@link Id} mapping.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@MappedSuperclass
public abstract class SimplePersistentObject extends PersistentObject
{
    @Override
    @Id
    @GeneratedValue
    public Long getId()
    {
        return id;
    }
}
