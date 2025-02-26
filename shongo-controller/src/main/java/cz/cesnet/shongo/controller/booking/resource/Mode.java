package cz.cesnet.shongo.controller.booking.resource;

import cz.cesnet.shongo.SimplePersistentObject;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Represents a mode of a device.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(indexes = { @Index(name = "connector_agent_name", columnList = "connector_agent_name") })
public abstract class Mode extends SimplePersistentObject
{
}
