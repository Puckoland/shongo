package cz.cesnet.shongo.controller.api;

import cz.cesnet.shongo.ParticipantRole;
import cz.cesnet.shongo.api.DataMap;

/**
 * {@link AbstractParticipant} for {@link AbstractPerson}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class PersonParticipant extends AbstractParticipant
{
    /**
     * The requested person.
     */
    private AbstractPerson person;

    /**
     * Each {@link AbstractParticipant} acts in a meeting in a {@link cz.cesnet.shongo.ParticipantRole}.
     */
    private ParticipantRole role;

    /**
     * Constructor.
     */
    public PersonParticipant()
    {
    }

    /**
     * Constructor.
     *
     * @param name  sets the {@link cz.cesnet.shongo.controller.api.AnonymousPerson#name} for the {@link #PERSON}
     * @param email sets the {@link cz.cesnet.shongo.controller.api.AnonymousPerson#email} for the {@link #PERSON}
     */
    public PersonParticipant(String name, String email)
    {
        setPerson(new AnonymousPerson(name, email));
    }

    /**
     * @return {@link #PERSON}
     */
    public AbstractPerson getPerson()
    {
        return person;
    }

    /**
     * @param person sets the {@link #PERSON}
     */
    public void setPerson(AbstractPerson person)
    {
        this.person = person;
    }

    /**
     * @return {@link #role}
     */
    public ParticipantRole getRole()
    {
        return role;
    }

    /**
     * @param role sets the {@link #role}
     */
    public void setRole(ParticipantRole role)
    {
        this.role = role;
    }

    public static final String PERSON = "person";
    public static final String ROLE = "role";

    @Override
    public DataMap toData()
    {
        DataMap dataMap = super.toData();
        dataMap.set(PERSON, person);
        dataMap.set(ROLE, role);
        return dataMap;
    }

    @Override
    public void fromData(DataMap dataMap)
    {
        super.fromData(dataMap);
        person = dataMap.getComplexTypeRequired(PERSON, AbstractPerson.class);
        role = dataMap.getEnum(ROLE, ParticipantRole.class);
    }
}